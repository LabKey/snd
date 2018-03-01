/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.snd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.cache.CacheManager;
import org.labkey.api.cache.StringKeyCache;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableResultSet;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.ObjectProperty;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.exp.property.DomainUtil;
import org.labkey.api.gwt.client.model.GWTDomain;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.AttributeData;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
import org.labkey.api.snd.Project;
import org.labkey.api.snd.ProjectItem;
import org.labkey.api.snd.SNDDomainKind;
import org.labkey.api.snd.SNDSequencer;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.DateUtil;
import org.labkey.api.util.GUID;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SNDManager
{
    private static final SNDManager _instance = new SNDManager();

    private final StringKeyCache<Object> _cache;

    private List<TableInfo> _attributeLookups = new ArrayList<>();

    public static final String RANGE_PARTICIPANTID = "ParticipantId";

    private SNDManager()
    {
        _cache = CacheManager.getStringKeyCache(1000, CacheManager.UNLIMITED, "SNDCache");
    }

    public static SNDManager get()
    {
        return _instance;
    }

    public StringKeyCache getCache()
    {
        return _cache;
    }

    public static String getPackageName(int id)
    {
        return PackageDomainKind.getPackageKindName() + "-" + id;
    }

    public void deletePackageCategories(Container c, User u, int pkgId)
    {
        SQLFragment sql = new SQLFragment("DELETE FROM " + SNDSchema.getInstance().getTableInfoPkgCategoryJunction());
        sql.append(" WHERE PkgId = ? AND Container = ? ");
        sql.add(pkgId).add(c);

        SqlExecutor sqlex = new SqlExecutor(SNDSchema.getInstance().getSchema());
        sqlex.execute(sql);
    }

    private TableInfo getTableInfo(@NotNull UserSchema schema, @NotNull String table)
    {
        TableInfo tableInfo = schema.getTable(table);
        if (tableInfo == null)
            throw new IllegalStateException(table + " TableInfo not found");

        return tableInfo;
    }

    private QueryUpdateService getQueryUpdateService(@NotNull TableInfo table)
    {
        QueryUpdateService qus = table.getUpdateService();
        if (qus == null)
            throw new IllegalStateException(table.getName() + " query update service");

        return qus;
    }

    // Creates a new QUS for extensible tables. Used for tables that have had their QUS blocked.
    private QueryUpdateService getNewQueryUpdateService(@NotNull UserSchema schema, @NotNull String table)
    {
        TableInfo dbTableInfo = schema.getDbSchema().getTable(table);
        if (dbTableInfo == null)
            throw new IllegalStateException(table + " db table info not found.");

        SimpleUserSchema.SimpleTable simpleTable = new SimpleUserSchema.SimpleTable(schema, dbTableInfo);
        QueryUpdateService qus = new SimpleQueryUpdateService(simpleTable, dbTableInfo);

        if (qus == null)
            throw new IllegalStateException(dbTableInfo.getName() + " query update service");

        return qus;
    }

    public Object getDefaultLookupDisplayValue(User u, Container c, String schema, String table, Object key)
    {
        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);

        TableInfo tableInfo = getTableInfo(userSchema, table);
        QueryUpdateService lookupQus = getQueryUpdateService(tableInfo);

        Map<String, Object> keyRow = new HashMap<>();
        String pk = tableInfo.getPkColumnNames().get(0);  // Only handling single value pks
        keyRow.put(pk, key);

        List<Map<String, Object>> pkRows = new ArrayList<>();
        pkRows.add(keyRow);

        List<Map<String, Object>> rows;
        try
        {
            rows = lookupQus.getRows(u, c, pkRows);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        if (rows == null || rows.size() < 1)
            return null;

        return rows.get(0).get(tableInfo.getTitleColumn());
    }

    public Object normalizeLookupDefaultValue(User u, Container c, String schema, String table, String display)
    {
        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);

        TableInfo tableInfo = getTableInfo(userSchema, table);
        String pk = tableInfo.getPkColumnNames().get(0);  // Only handling single value pks

        SQLFragment sql = new SQLFragment("SELECT " + pk + " FROM ");
        sql.append(tableInfo, "l");
        sql.append(" WHERE " + tableInfo.getTitleColumn() + " = ?").add(display);
        SqlSelector selector = new SqlSelector(userSchema.getDbSchema(), sql);
        List<Object> lookupRows = selector.getArrayList(Object.class);

        if (lookupRows.size() < 1)
            return null;

        return lookupRows.get(0);
    }

    public void updatePackage(User u, Container c, @NotNull Package pkg, @Nullable SuperPackage superPkg, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = getTableInfo(schema, SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = getQueryUpdateService(pkgsTable);

        TableInfo pkgCategJuncTable = getTableInfo(schema, SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME);
        QueryUpdateService pkgCategoryQus = getQueryUpdateService(pkgCategJuncTable);

        List<Map<String, Object>> pkgRows = new ArrayList<>();
        pkgRows.add(pkg.getPackageRow(c));

        try (DbScope.Transaction tx = pkgsTable.getSchema().getScope().ensureTransaction())
        {
            pkgQus.updateRows(u, c, pkgRows, null, null, null);

            // For categories delete existing junction relations and add new ones
            deletePackageCategories(c, u, pkg.getPkgId());
            pkgCategoryQus.insertRows(u, c, pkg.getCategoryRows(c), errors, null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | InvalidKeyException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        // If package is in use (either assigned to an event or project) then do not update the domain
        if (!errors.hasErrors() && !((PackagesTable) pkgsTable).isPackageInUse(pkg.getPkgId()))
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), getPackageName(pkg.getPkgId()), c, u);

            GWTDomain<GWTPropertyDescriptor> updateDomain = new GWTDomain<>();
            updateDomain.setName(getPackageName(pkg.getPkgId()));
            updateDomain.setFields(pkg.getAttributes());
            updateDomain.setDomainURI(domainURI);

            PackageDomainKind kind = new PackageDomainKind();
            kind.updateDomain(c, u, updateDomain);
        }

        // Super packages null when importing xml
        if (superPkg != null)
        {
            superPkg.setChildPackages(pkg.getSubpackages());

            List<SuperPackage> saves = new ArrayList<>();
            saves.add(superPkg);
            saveSuperPackages(u, c, saves, errors);
        }
    }

    public void createPackage(User u, Container c, @NotNull Package pkg, @Nullable SuperPackage superPkg, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = getTableInfo(schema, SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = getQueryUpdateService(pkgsTable);

        TableInfo pkgCategJuncTable = getTableInfo(schema, SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME);
        QueryUpdateService pkgCategoryQus = getQueryUpdateService(pkgCategJuncTable);

        List<Map<String, Object>> pkgRows = new ArrayList<>();
        pkgRows.add(pkg.getPackageRow(c));

        try (DbScope.Transaction tx = pkgsTable.getSchema().getScope().ensureTransaction())
        {
            pkgQus.insertRows(u, c, pkgRows, errors, null, null);
            pkgCategoryQus.insertRows(u, c, pkg.getCategoryRows(c), errors, null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        if (!errors.hasErrors())
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), getPackageName(pkg.getPkgId()), c, u);
            GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, domainURI, c);

            if (domain == null)
                throw new IllegalStateException("Cannot save package attributes. Domain not found.");

            domain.setFields(pkg.getAttributes());
            PackageDomainKind kind = new PackageDomainKind();
            kind.updateDomain(c, u, domain);

            // Super packages null when importing xml
            if (superPkg != null)
            {
                if (pkg.getSubpackages() != null)
                {
                    superPkg.setChildPackages(pkg.getSubpackages());
                }

                List<SuperPackage> saves = new ArrayList<>();
                saves.add(superPkg);
                saveSuperPackages(u, c, saves, errors);
            }
        }
    }

    private List<Integer> getSavedSuperPkgs(Container c, User u)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "s");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.getArrayList(Integer.class);
    }

    private void deleteRemovedChildren(User u, Container c, SuperPackage superPkg, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo superPkgsTable = getTableInfo(schema, SNDSchema.SUPERPKGS_TABLE_NAME);
        QueryUpdateService superPkgQus = getQueryUpdateService(superPkgsTable);

        List<Integer> superPackageIdsToDelete = getDeletedChildSuperPkgs(c, u, superPkg.getChildPackages(), superPkg.getSuperPkgId());
        if (superPackageIdsToDelete != null)
        {
            List<Map<String, Object>> superPackageRows = new ArrayList<>();
            for (Integer superPackageId : superPackageIdsToDelete)
            {
                Map<String, Object> superPackageRow = new HashMap<>(1);
                superPackageRow.put("SuperPkgId", superPackageId);
                superPackageRows.add(superPackageRow);
            }
            try
            {
                superPkgQus.deleteRows(u, c, superPackageRows, null, null);
            }
            catch (QueryUpdateServiceException | BatchValidationException | SQLException | InvalidKeyException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }
        }
    }

    public void saveSuperPackages(User u, Container c, List<SuperPackage> superPkgs, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo superPkgsTable = getTableInfo(schema, SNDSchema.SUPERPKGS_TABLE_NAME);
        QueryUpdateService superPkgQus = getQueryUpdateService(superPkgsTable);

        // Flatten packages, combine children and parent in same list
        List<SuperPackage> flatSuperPackages = new ArrayList<>();
        for (SuperPackage parent : superPkgs)
        {
            flatSuperPackages.add(parent);
            if (parent.getChildPackages() != null)
            {
                for (SuperPackage child : parent.getChildPackages())
                {
                    flatSuperPackages.add(child);
                }
            }
        }

        List<Integer> savedSuperPkgs = getSavedSuperPkgs(c, u);
        List<Map<String, Object>> updates = new ArrayList<>();
        List<Map<String, Object>> inserts = new ArrayList<>();

        // Update existing rows and add new rows
        for (SuperPackage superPackage : flatSuperPackages)
        {
            superPackage.setSuperPkgPath(Integer.toString(superPackage.getSuperPkgId()));
            if (savedSuperPkgs.contains(superPackage.getSuperPkgId()))
            {
                updates.add(superPackage.getSuperPackageRow(c));
            }
            else
            {
                inserts.add(superPackage.getSuperPackageRow(c));
            }
        }

        try (DbScope.Transaction tx = superPkgsTable.getSchema().getScope().ensureTransaction())
        {
            superPkgQus.insertRows(u, c, inserts, errors, null, null);
            superPkgQus.updateRows(u, c, updates, null, null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | InvalidKeyException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        // now delete orphaned child super packages
        for (SuperPackage superPkg : superPkgs)
        {
            deleteRemovedChildren(u, c, superPkg, errors);
        }
    }

    private List<GWTPropertyDescriptor> getPackageAttributes(Container c, User u, int pkgId)
    {
        String uri = PackageDomainKind.getDomainURI(SNDSchema.NAME, getPackageName(pkgId), c, u);
        GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, uri, c);
        if (domain != null)
            return domain.getFields();

        return Collections.emptyList();
    }


    private List<Integer> getPackageCategories(Container c, User u, int pkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT CategoryId FROM ");
        sql.append(schema.getTable(SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME), "c");
        sql.append(" WHERE PkgId = ?").add(pkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.getArrayList(Integer.class);
    }

    private List<GWTPropertyDescriptor> getExtraFields(Container c, User u, String tableName)
    {
        String uri = SNDDomainKind.getDomainURI(SNDSchema.NAME, tableName, c, u);
        GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, uri, c);
        if (domain != null)
            return domain.getFields();

        return Collections.emptyList();
    }

    public Package addExtraFieldsToPackage(Container c, User u, Package pkg, @Nullable Map<String, Object> row)
    {
        List<GWTPropertyDescriptor> extraFields = getExtraFields(c, u, SNDSchema.PKGS_TABLE_NAME);
        Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
        for (GWTPropertyDescriptor extraField : extraFields)
        {
            if (row == null)
            {
                extras.put(extraField, "");
            }
            else
            {
                extras.put(extraField, row.get(extraField.getName()));
            }
        }
        pkg.setExtraFields(extras);

        return pkg;
    }

    private SuperPackage addChildren(SuperPackage parent, List<SuperPackage> descendants)
    {
        List<SuperPackage> children = new ArrayList<>();

        for (SuperPackage child : descendants)
        {
            if (child.getParentSuperPkgId() != null
                    && (child.getParentSuperPkgId().intValue() == parent.getSuperPkgId().intValue()))
            {
                children.add(addChildren(child, descendants));
            }
        }

        parent.setChildPackages(children);
        return parent;
    }

    // return all super package IDs which correspond to this package ID
    @Nullable
    public static List<Integer> getSuperPkgIdsForPkg(Container c, User u, Integer packageId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.PkgId = ?").add(packageId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() > 0)
            return selector.getArrayList(Integer.class);
        else
            return null;
    }

    // return the top-level super package which corresponds to this package ID
    @Nullable
    public static SuperPackage getTopLevelSuperPkgForPkg(Container c, User u, Integer packageId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.ParentSuperPkgId, sp.PkgId, sp.SuperPkgPath, sp.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.PkgId = ?").add(packageId);
        sql.append(" AND sp.ParentSuperPkgId IS NULL");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() == 1)
            return selector.getArrayList(SuperPackage.class).get(0);
        else
            return null;
    }

    // return the super packages which correspond to these super package IDs
    @Nullable
    public static List<SuperPackage> getSuperPkgs(Container c, User u, List<Integer> superPackageIds)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.ParentSuperPkgId, sp.PkgId, sp.SuperPkgPath, sp.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.SuperPkgId IN (");
        addSubPkgParameters(sql, superPackageIds.iterator());
        sql.append(")");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() > 0)
            return selector.getArrayList(SuperPackage.class);
        else
            return null;
    }

    // convert all passed-in super packages to distinct top-level super packages
    @Nullable
    public static List<SuperPackage> convertToTopLevelSuperPkgs(Container c, User u, List<Integer> superPackageIds)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT DISTINCT sp2.SuperPkgId, sp2.ParentSuperPkgId, sp2.PkgId, sp2.SuperPkgPath, sp2.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" JOIN ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp2");
        sql.append(" ON sp2.PkgId = sp.PkgId");

        sql.append(" WHERE sp.SuperPkgId IN (");
        addSubPkgParameters(sql, superPackageIds.iterator());
        sql.append(") AND sp2.ParentSuperPkgId IS NULL");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() > 0)
            return selector.getArrayList(SuperPackage.class);
        else
            return null;
    }

    // filters list of superPackageIds down to super packages which have no parent
    @Nullable
    public static List<SuperPackage> filterTopLevelSuperPkgs(Container c, User u, List<Integer> superPackageIds)
    {
        if ((superPackageIds == null) || (superPackageIds.size() == 0))
            return null;

        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.ParentSuperPkgId, sp.PkgId, sp.SuperPkgPath, sp.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.SuperPkgId IN (");
        addSubPkgParameters(sql, superPackageIds.iterator());
        sql.append(") AND sp.ParentSuperPkgId IS NULL");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() > 0)
            return selector.getArrayList(SuperPackage.class);
        else
            return null;
    }

    // only gets IDs for all child super packages that point to a certain super package ID
    @Nullable
    public static List<SuperPackage> getChildSuperPkgs(Container c, User u, Integer parentSuperPackageId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.ParentSuperPkgId, sp.PkgId, sp.SuperPkgPath, sp.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.ParentSuperPkgId = ?").add(parentSuperPackageId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(Integer.class).size() > 0)
            return selector.getArrayList(SuperPackage.class);
        else
            return null;
    }

    // filters list of superPackageIds down to super packages that have parentSuperPackageId as a parent
    @Nullable
    public static List<SuperPackage> filterChildSuperPkgs(Container c, User u, List<Integer> superPackageIds, Integer parentSuperPackageId)
    {
        if ((superPackageIds == null) || (superPackageIds.size() == 0))
            return null;

        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.ParentSuperPkgId, sp.PkgId, sp.SuperPkgPath, sp.SortOrder FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE sp.SuperPkgId IN (");
        addSubPkgParameters(sql, superPackageIds.iterator());
        sql.append(") AND sp.ParentSuperPkgId = ?").add(parentSuperPackageId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(SuperPackage.class).size() > 0)
            return selector.getArrayList(SuperPackage.class);
        else
            return null;
    }

    // return list of super package IDs that should be deleted based on passed-in superPackageIds and parentSuperPackage ID
    @Nullable
    public static List<Integer> getDeletedChildSuperPkgs(Container c, User u, List<SuperPackage> superPackages, Integer parentSuperPackageId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE");
        if ((superPackages != null) && (superPackages.size() > 0))
        {
            sql.append(" sp.SuperPkgId NOT IN (");
            Iterator<SuperPackage> superPackageIterator = superPackages.iterator();
            while (superPackageIterator.hasNext())
            {
                Integer superPkgId = superPackageIterator.next().getSuperPkgId();
                if (!superPackageIterator.hasNext())
                    sql.append("?").add(superPkgId);
                else
                    sql.append("?,").add(superPkgId);
            }
            sql.append(") AND");
        }
        sql.append(" sp.ParentSuperPkgId = ?").add(parentSuperPackageId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(Integer.class).size() > 0)
            return selector.getArrayList(Integer.class);
        else
            return null;
    }

    private static void addSubPkgParameters(SQLFragment sql, Iterator<Integer> subPkgIterator)
    {
        while (subPkgIterator.hasNext())
        {
            Integer superPkgId = subPkgIterator.next();
            if (!subPkgIterator.hasNext())
                sql.append("?").add(superPkgId);
            else
                sql.append("?,").add(superPkgId);
        }
    }

    public boolean isDescendent(Container c, User u, int topLevelSuperPkgId, int pkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT PkgId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_TABLE_NAME);
        sql.append(" WHERE SuperPkgId = ?");
        sql.add(topLevelSuperPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        Integer topLevelPkgId = selector.getArrayList(Integer.class).get(0);

        sql = new SQLFragment("SELECT * FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_FUNCTION_NAME + "(?)");
        sql.append(" WHERE PkgId = ?");
        sql.add(topLevelPkgId).add(pkgId);

        selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.getRowCount() > 0;
    }

    @Nullable
    private SuperPackage getFullSuperPackage(Container c, User u, int superPkgId, boolean getFullSubpackages)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.PkgId, sp.SortOrder, pkg.PkgId, pkg.Description, pkg.Active, pkg.Narrative, pkg.Repeatable FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_TABLE_NAME + " sp");
        sql.append(" JOIN " + SNDSchema.NAME + "." + SNDSchema.PKGS_TABLE_NAME + " pkg");
        sql.append(" ON sp.PkgId = pkg.PkgId");
        sql.append(" WHERE sp.SuperPkgId = ?");
        sql.add(superPkgId);

        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        SuperPackage superPackage = selector.getObject(SuperPackage.class);

        if (getFullSubpackages)
        {
            Package pkg;
            pkg = new Package();
            pkg.setAttributes(getPackageAttributes(c, u, superPackage.getPkgId()));
            pkg.setPkgId(superPackage.getPkgId());
            pkg.setDescription(superPackage.getDescription());
            superPackage.setPkg(pkg);
        }

        if (superPackage != null)
            superPackage.setChildPackages(getAllChildSuperPkgs(c, u, superPackage.getPkgId(), getFullSubpackages));

        return superPackage;
    }

    // recursively get all children for the super package which corresponds to pkgId
    private List<SuperPackage> getAllChildSuperPkgs(Container c, User u, int pkgId, boolean getAllAttributes)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment childSql = new SQLFragment("SELECT * FROM ");
        childSql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_FUNCTION_NAME + "(?)").add(pkgId);

        SqlSelector selector = new SqlSelector(schema.getDbSchema(), childSql);
        List<SuperPackage> descendants = selector.getArrayList(SuperPackage.class);
        List<SuperPackage> children = new ArrayList<>();

        SuperPackage root = null;
        Package childPkg;
        for (SuperPackage sPkg : descendants)
        {
            if (getAllAttributes)
            {
                childPkg = new Package();
                childPkg.setAttributes(getPackageAttributes(c, u, sPkg.getPkgId()));
                childPkg.setPkgId(sPkg.getPkgId());
                childPkg.setDescription(sPkg.getDescription());
                sPkg.setPkg(childPkg);
            }

            if (sPkg.getParentSuperPkgId() == null)
            {
                root = sPkg;
            }
        }

        if (root != null)
        {
            for (SuperPackage descendent : descendants)
            {
                if ((descendent.getParentSuperPkgId() != null) &&
                        (descendent.getParentSuperPkgId().intValue() == root.getSuperPkgId().intValue()))
                {
                    children.add(addChildren(descendent, descendants));
                }
            }
        }

        return children;
    }

    private Package addSubPackagesToPackage(Container c, User u, Package pkg, boolean getAllAttributes)
    {
        pkg.setSubpackages(getAllChildSuperPkgs(c, u, pkg.getPkgId(), getAllAttributes));

        return pkg;
    }

    public Package addLookupsToPkg(Container c, User u, Package pkg)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        Map<String, Map<String, Object>> sndLookups = ((SNDUserSchema) schema).getLookupSets();
        Map<String, String> lookups = new HashMap<>();

        String key, label;
        for (String sndLookup : sndLookups.keySet())
        {
            key = "snd." + sndLookup;
            label = ((String) sndLookups.get(sndLookup).get("Label"));
            if (label != null)
            {
                lookups.put(key, label);
            }
            else
            {
                lookups.put(key, sndLookup);
            }
        }

        for (TableInfo ti : _attributeLookups)
        {
            key = ti.getSchema().getName() + "." + ti.getName();
            lookups.put(key, ti.getTitle());
        }

        pkg.setLookups(lookups);

        return pkg;
    }

    private Package createPackage(Container c, User u, Map<String, Object> row, boolean includeExtraFields, boolean includeLookups, boolean includeAllAttributes)
    {
        Package pkg = new Package();
        if (row != null)
        {
            pkg.setPkgId((Integer) row.get(Package.PKG_ID));
            pkg.setDescription((String) row.get(Package.PKG_DESCRIPTION));
            pkg.setActive((boolean) row.get(Package.PKG_ACTIVE));
            pkg.setRepeatable((boolean) row.get(Package.PKG_REPEATABLE));
            pkg.setNarrative((String) row.get(Package.PKG_NARRATIVE));
            pkg.setQcState((Integer) row.get(Package.PKG_QCSTATE));
            pkg.setHasEvent((boolean) row.get(Package.PKG_HASEVENT));
            pkg.setHasProject((boolean) row.get(Package.PKG_HASPROJECT));
            pkg.setCategories(getPackageCategories(c, u, pkg.getPkgId()));
            pkg.setAttributes(getPackageAttributes(c, u, pkg.getPkgId()));

            SuperPackage sPkg = getTopLevelSuperPkgForPkg(c, u, pkg.getPkgId());
            if (sPkg != null)
                pkg.setTopLevelSuperPkgId(sPkg.getSuperPkgId());

            addSubPackagesToPackage(c, u, pkg, includeAllAttributes);
            if (includeExtraFields)
                addExtraFieldsToPackage(c, u, pkg, row);
            if (includeLookups)
                addLookupsToPkg(c, u, pkg);
        }

        return pkg;
    }

    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, boolean includeExtraFields, boolean includeLookups,
                                     boolean includeAllAttributes, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = getTableInfo(schema, SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = getQueryUpdateService(pkgsTable);

        List<Map<String, Object>> rows = null;
        List<Map<String, Object>> keys = new ArrayList<>();
        Map<String, Object> key;
        for (Integer pkgId : pkgIds)
        {
            key = new HashMap<>();
            key.put("PkgId", pkgId);
            keys.add(key);
        }

        List<Package> packages = new ArrayList<>();
        try
        {
            rows = pkgQus.getRows(u, c, keys);
        }
        catch (InvalidKeyException | QueryUpdateServiceException | SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        if (!errors.hasErrors() && rows != null && !rows.isEmpty())
        {
            for (Map<String, Object> row : rows)
            {
                packages.add(createPackage(c, u, row, includeExtraFields, includeLookups, includeAllAttributes));
            }
        }

        return packages;
    }

    public boolean projectRevisionIsLatest(Container c, User u, int id, int rev)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ProjectId = ? AND RevisionNum > ?");
        sql.add(id).add(rev);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.getRowCount() < 1;
    }

    private boolean projectRevisionExists(Container c, User u, int id, int rev)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(id).add(rev);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.getRowCount() > 0;
    }

    private boolean hasOverlap(Project project, Map<String, Object> row, boolean revision, BatchValidationException errors)
    {
        int rowRev = (int) row.get("RevisionNum");
        int projectId = (int) row.get("ProjectId");
        boolean overlap = false;
        Date rowStart = null, rowEnd = null;

        // Check for overlapping dates
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            rowStart = formatter.parse((String) row.get("StartDate"));
            rowEnd = null;

            // For revisions we get enddate of incoming revised end date if comparing with revised project revision
            if (revision && ((Integer)row.get("RevisionNum") == project.getRevisionNum()))
            {
                rowEnd = project.getEndDateRevised();
            }
            else if (row.get("EndDate") != null)
            {
                rowEnd = formatter.parse((String) row.get("EndDate"));
            }
        }
        catch (ParseException e)
        {
            errors.addRowError(new ValidationException("Unable to parse date. " + e.getMessage()));
        }

        if (rowStart != null)
        {
            // Don't compare to the current row unless its a revision
            if (revision || rowRev != project.getRevisionNum() || projectId != project.getProjectId())
            {
                // Overlap scenarios
                if (project.getStartDate().equals(rowStart))
                {
                    overlap = true;
                }
                else if (project.getStartDate().after(rowStart))
                {
                    if (rowEnd == null)
                    {
                        overlap = true;
                    }
                    else if (project.getStartDate().before(rowEnd))
                    {
                        overlap = true;
                    }
                }
                else if (rowStart.after(project.getStartDate()))
                {
                    if (project.getEndDate() == null)
                    {
                        overlap = true;
                    }
                    else if (rowStart.before(project.getEndDate()))
                    {
                        overlap = true;
                    }
                }
            }
        }

        return overlap;
    }

    private boolean isValidReferenceId(Container c, User u, Project project, boolean revision, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        TableInfo projectTable = getTableInfo(schema, SNDSchema.PROJECTS_TABLE_NAME);
        boolean valid = true;
        List<Map<String, Object>> rows;
        SimpleFilter filter;

        // First ensure if the referenceId is being updated that it is not an in use project
        if (!revision)
        {
            rows = new ArrayList<>();

            filter = new SimpleFilter(FieldKey.fromString("ProjectId"), project.getProjectId(), CompareType.EQUAL);
            filter.addCondition(FieldKey.fromString("RevisionNum"), project.getRevisionNum(), CompareType.EQUAL);
            TableSelector ts = new TableSelector(projectTable, filter, null);
            try(TableResultSet rs = ts.getResultSet())
            {
                for (Map<String, Object> r : rs)
                {
                    rows.add(r);
                }

                if (rows.size() > 0)
                {
                    Map<String, Object> row = rows.get(0);
                    if ((Integer) row.get("ReferenceId") != project.getReferenceId() && Boolean.parseBoolean((String)row.get("HasEvent")))
                    {
                        errors.addRowError(new ValidationException("This is an in use project. Reference Id cannot be changed."));
                        valid = false;
                    }
                }
            }
            catch (SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
                valid = false;
            }
        }

        // Second ensure project does not overlap other projects with same referenceId
        if (valid)
        {
            rows = new ArrayList<>();
            filter = new SimpleFilter(FieldKey.fromString("ReferenceId"), project.getReferenceId(), CompareType.EQUAL);
            TableSelector ts = new TableSelector(projectTable, filter, null);
            try(TableResultSet rs = ts.getResultSet())
            {
                for (Map<String, Object> r : rs)
                {
                    rows.add(r);
                }

                if (rows.size() > 0)
                {
                    for (Map<String, Object> row : rows)
                    {
                        // Check for overlapping dates
                        if (hasOverlap(project, row, revision, errors))
                        {
                            errors.addRowError(new ValidationException("Overlapping use of Reference Id with Project Id "
                                    + row.get("ProjectId") + ", revision " + row.get("RevisionNum")));
                            valid = false;
                            break;
                        }
                    }
                }
            }
            catch (SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
                valid = false;
            }
        }
        return valid;
    }

    private boolean isValidRevision(Container c, User u, Project project, boolean revision, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date endDate;

        SQLFragment sql = new SQLFragment("SELECT ProjectId, RevisionNum, StartDate, EndDate FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ProjectId = ?");
        sql.add(project.getProjectId());
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        // If creating project for first time revNum is zero and not a revision
        boolean validRevision = (project.getRevisionNum() == 0 && !revision), overlap = false;
        Integer rev;
        String end;

        try(TableResultSet rows = selector.getResultSet())
        {
            // Iterate through rows to check date overlap and ensure revision is incremented properly
            for (Map<String, Object> row : rows)
            {
                rev = (Integer) row.get("RevisionNum");

                // Check for overlapping dates
                if (!overlap && hasOverlap(project, row, revision, errors))
                {
                    errors.addRowError(new ValidationException("Overlapping date with revision " + row.get("RevisionNum") + " of this project."));
                    overlap = true;
                }

                // Verify revision numbers are sequential
                if ((revision && project.getRevisedRevNum() == (rev + 1))
                        || project.getRevisionNum() == (rev + 1))
                    validRevision = true;

                if (revision && rev == project.getRevisionNum())
                {
                    end = DateUtil.formatDateISO8601(project.getEndDateRevised());
                }
                else
                {
                    end = (String) row.get("EndDate");
                }

                // Check previous revisions to verify only the latest revision of a project has a null end date
                if (end == null)
                {
                    if (rev < project.getRevisionNum() || (revision && rev == project.getRevisionNum()))
                    {
                        errors.addRowError(new ValidationException("Only the latest revision of a project may have a null date."));
                    }
                }

                // Verify only last revision can have null date on edit project
                if (project.getEndDate() == null)
                {
                    if (rev > project.getRevisionNum())
                    {
                        errors.addRowError(new ValidationException("Only the latest revision of a project may have a null date."));
                    }
                }

                // Verify endDates of previous revisions are before this revision begins
                if (end != null && (revision || rev < project.getRevisionNum()))
                {
                    endDate = formatter.parse(end);
                    if (endDate.after(project.getStartDate()))
                    {
                        errors.addRowError(new ValidationException("Start date must be after the end date of previous revisions."));
                    }
                }
            }

            if (!validRevision)
                errors.addRowError(new ValidationException("Invalid revision number."));
        }
        catch (SQLException | ParseException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        return errors.hasErrors();
    }

    public boolean validProject(Container c, User u, Project project, boolean revision, BatchValidationException errors)
    {
        if (revision && projectRevisionExists(c, u, project.getProjectId(), project.getRevisedRevNum()))
        {
            errors.addRowError(new ValidationException("Revision " + project.getRevisedRevNum() + " already exists for this project. Can only make revision from latest revision."));
        }

        isValidRevision(c, u, project, revision, errors);
        isValidReferenceId(c, u, project, revision, errors);

        return !errors.hasErrors();

    }

    public void createProject(Container c, User u, Project project, BatchValidationException errors)
    {
        if (validProject(c, u, project, false, errors))
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

            TableInfo projectTable = getTableInfo(schema, SNDSchema.PROJECTS_TABLE_NAME);
            QueryUpdateService projectQus = getQueryUpdateService(projectTable);

            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);
            QueryUpdateService projectItemsQus = getQueryUpdateService(projectItemsTable);

            List<Map<String, Object>> projectRows = new ArrayList<>();
            projectRows.add(project.getProjectRow(c));

            try (DbScope.Transaction tx = projectTable.getSchema().getScope().ensureTransaction())
            {
                projectQus.insertRows(u, c, projectRows, errors, null, null);
                projectItemsQus.insertRows(u, c, project.getProjectItemRows(c), errors, null, null);
                tx.commit();
            }
            catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }
        }
    }

    private void updateProjectField(Container c, User u, int id, int rev, String field, String value)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("UPDATE ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" SET " + field + " = ?");
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(value).add(id).add(rev);

        new SqlExecutor(schema.getDbSchema().getScope()).execute(sql);
    }

    public void reviseProject(Container c, User u, Project project, BatchValidationException errors)
    {
        if (validProject(c, u, project, true, errors))
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
            List<Map<String, Object>> updatedProjectItems = new ArrayList<>();

            updateProjectField(c, u, project.getProjectId(), project.getRevisionNum(), "EndDate",
                    project.getEndDateRevised() == null ? null : DateUtil.toISO(project.getEndDateRevised()));

            if (project.isCopyRevisedPkgs())
            {
                // First get copy of the project items from the original project
                SQLFragment sql = new SQLFragment("SELECT SuperPkgId, Active FROM ");
                sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTITEMS_TABLE_NAME);
                sql.append(" WHERE ParentObjectId = ?");
                sql.add(project.getObjectId());
                SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

                // Update project items with new parentobjectid
                try (TableResultSet projectItems = selector.getResultSet())
                {
                    for (Map<String, Object> row : projectItems)
                    {
                        row.put("ParentObjectId", project.getRevisedObjectId());
                        updatedProjectItems.add(row);
                    }
                }
                catch (SQLException e)
                {
                    errors.addRowError(new ValidationException(e.getMessage()));
                }
            }

            // Set project objectid and revision
            project.setObjectId(project.getRevisedObjectId());
            project.setRevisionNum(project.getRevisedRevNum());

            TableInfo projectTable = getTableInfo(schema, SNDSchema.PROJECTS_TABLE_NAME);
            QueryUpdateService projectQus = getQueryUpdateService(projectTable);

            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);
            QueryUpdateService projectItemsQus = getQueryUpdateService(projectItemsTable);

            List<Map<String, Object>> projectRows = new ArrayList<>();
            projectRows.add(project.getProjectRow(c));

            // Create revised projects and insert copies of project items
            try (DbScope.Transaction tx = projectTable.getSchema().getScope().ensureTransaction())
            {
                projectQus.insertRows(u, c, projectRows, errors, null, null);
                if (project.isCopyRevisedPkgs())
                    projectItemsQus.insertRows(u, c, updatedProjectItems, errors, null, null);
                tx.commit();
            }
            catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }
        }
    }

    public void updateProject(Container c, User u, Project project, BatchValidationException errors)
    {
        if (validProject(c, u, project, false, errors))
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

            // Erase all project items for this project
            SQLFragment sql = new SQLFragment("DELETE FROM ");
            sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTITEMS_TABLE_NAME);
            sql.append(" WHERE ParentObjectId = ?");
            sql.add(project.getObjectId());

            SqlExecutor executor = new SqlExecutor(schema.getDbSchema());
            executor.execute(sql);

            //Update and insert new project items
            TableInfo projectTable = getTableInfo(schema, SNDSchema.PROJECTS_TABLE_NAME);
            QueryUpdateService projectQus = getQueryUpdateService(projectTable);

            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);
            QueryUpdateService projectItemsQus = getQueryUpdateService(projectItemsTable);

            List<Map<String, Object>> projectRows = new ArrayList<>();
            projectRows.add(project.getProjectRow(c));

            try (DbScope.Transaction tx = projectTable.getSchema().getScope().ensureTransaction())
            {
                projectQus.updateRows(u, c, projectRows, null, null, null);
                projectItemsQus.insertRows(u, c, project.getProjectItemRows(c), errors, null, null);
                tx.commit();
            }
            catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | InvalidKeyException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }
        }
    }

    public String getProjectObjectId(Container c, User u, Project project, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ObjectId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(project.getProjectId()).add(project.getRevisionNum());
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        if (selector.getRowCount() < 1)
            return null;

        return selector.getArrayList(String.class).get(0);
    }

    public List<Map<String, Object>> getProjectItems(Container c, User u, int projectId, int revNum)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectItemId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTITEMS_TABLE_NAME + " pi");
        sql.append(" JOIN " + SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME + " pr");
        sql.append(" ON pi.ParentObjectId = pr.ObjectId");
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(projectId).add(revNum);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        List<Map<String, Object>> projectItems = new ArrayList<>();
        try(TableResultSet rs = selector.getResultSet())
        {

            for (Map<String, Object> row : rs)
            {
                projectItems.add(row);
            }
        }
        catch (SQLException e)
        {
            // swallow
        }

        return projectItems;
    }

    public Project addExtraFieldsToProject(Container c, User u, Project project, @Nullable Map<String, Object> row)
    {
        List<GWTPropertyDescriptor> extraFields = getExtraFields(c, u, SNDSchema.PROJECTS_TABLE_NAME);
        Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
        for (GWTPropertyDescriptor extraField : extraFields)
        {
            if (row == null)
            {
                extras.put(extraField, "");
            }
            else
            {
                extras.put(extraField, row.get(extraField.getName()));
            }
        }
        project.setExtraFields(extras);

        return project;
    }

    public Project getProject(Container c, User u, int projectId, int revNum)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo projectsTable = getTableInfo(schema, SNDSchema.PROJECTS_TABLE_NAME);

        // Get from projects table
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("ProjectId"), projectId, CompareType.EQUAL);
        filter.addCondition(FieldKey.fromParts("RevisionNum"), revNum, CompareType.EQUAL);
        TableSelector ts = new TableSelector(projectsTable, filter, null);

        // Unique constraint enforces only one project for projectId/revisionNum
        Project project = ts.getObject(Project.class);
        if (project != null)
        {

            // Get projectItems
            // TODO: If there are perf issues we may be able to use a simpler query
            SQLFragment sql = new SQLFragment("SELECT * FROM ");
            sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_FUNCTION_NAME + "(?, ?)");
            sql.append(" WHERE Level = 1").add(projectId).add(revNum);
            SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

            List<ProjectItem> pItems = new ArrayList<>();
            SuperPackage superPackage;
            for (ProjectItem projectItem : selector.getArrayList(ProjectItem.class))
            {
                superPackage = getFullSuperPackage(c, u, projectItem.getSuperPkgId(), false);
                if (superPackage != null)
                {
                    projectItem.setSuperPackage(superPackage);
                    pItems.add(projectItem);
                }
            }
            project.setProjectItems(pItems);

            // Extensible columns
            addExtraFieldsToProject(c, u, project, ts.getMap());
        }

        return project;

    }

    public void registerAttributeLookups(Container c, User u, String schema, String table)
    {
        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);
        if (table == null || table.isEmpty())
        {
            _attributeLookups.addAll(userSchema.getTables());
        }
        else
        {
            _attributeLookups.add(userSchema.getTable(table));
        }
    }

    public Map<String, String> getAttributeLookups(Container c, User u)
    {
        Map<String, String> tables = new HashMap<>();
        String key;
        for (TableInfo ti : _attributeLookups)
        {
            key = ti.getSchema().getName() + "." + ti.getName();
            tables.put(key, ti.getTitle());
        }

        return tables;
    }

    private EventData getEventData(Container c, User u, int eventDataId, SuperPackage superPackage, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        TableInfo eventDataTable = getTableInfo(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        // Get from EventData table
        SimpleFilter filter = new SimpleFilter(FieldKey.fromParts("EventDataId"), eventDataId, CompareType.EQUAL);
        TableSelector ts = new TableSelector(eventDataTable, filter, null);

        EventData eventData = ts.getObject(EventData.class);

        OntologyManager.getProperties(c, eventData.getObjectURI());
        Map<String, ObjectProperty> properties = OntologyManager.getPropertyObjects(c, eventData.getObjectURI());

        List<AttributeData> attributeDatas = new ArrayList<>();
        AttributeData attribute;
        for (GWTPropertyDescriptor gwtPropertyDescriptor : superPackage.getPkg().getAttributes())
        {
            attribute = new AttributeData();
            attribute.setPropertyDescriptor(gwtPropertyDescriptor);
            attribute.setPropertyId(gwtPropertyDescriptor.getPropertyId());
            if (properties.get(gwtPropertyDescriptor.getPropertyURI()) != null)
                attribute.setValue(properties.get(gwtPropertyDescriptor.getPropertyURI()).value().toString());

            attributeDatas.add(attribute);
        }

        eventData.setAttributes(attributeDatas);
        eventData.setNarrative(superPackage.getNarrative());
        addExtraFieldsToEventData(c, u, eventData, ts.getMap());

        SQLFragment sql = new SQLFragment("SELECT EventDataId, SuperPkgId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.EVENTDATA_TABLE_NAME);
        sql.append(" WHERE ParentEventDataId = ?").add(eventDataId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        TableResultSet results = selector.getResultSet();
        Integer superPkgId;
        SuperPackage eventDataSuperPkg = null;
        List<EventData> subEventDatas = new ArrayList<>();

        for (Map<String, Object> result : results)
        {
            superPkgId = (Integer)result.get("SuperPkgId");
            for (SuperPackage supPkg : superPackage.getChildPackages())
            {
                if (supPkg.getSuperPkgId().equals(superPkgId))
                {
                    eventDataSuperPkg = supPkg;
                    break;
                }
            }

            if (eventDataSuperPkg != null)
            {
                subEventDatas.add(getEventData(c, u, (Integer)result.get("EventDataId"), eventDataSuperPkg, errors));
            }
            else
            {
                errors.addRowError(new ValidationException("Super package not found for event data."));
            }
        }
        eventData.setSubPackages(subEventDatas);

        return eventData;
    }

    private List<EventData> getEventDatas(Container c, User u, int eventId, BatchValidationException errors)
    {
        List<EventData> eventDatas = new ArrayList<>();

        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT SuperPkgId, EventDataId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.EVENTDATA_TABLE_NAME);
        sql.append(" WHERE EventId = ? AND ParentEventDataId IS NULL").add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        Map<Integer, SuperPackage> topLevelSuperPackages = new HashMap<>();
        Integer superPkgId;

        TableResultSet results = selector.getResultSet();
        for (Map<String, Object> result : results)
        {
            superPkgId = (Integer)result.get("SuperPkgId");
            if (!topLevelSuperPackages.containsKey(superPkgId))
            {
                topLevelSuperPackages.put(superPkgId, getFullSuperPackage(c, u, superPkgId, true));
            }

            eventDatas.add(getEventData(c, u, (Integer)result.get("EventDataId"), topLevelSuperPackages.get(superPkgId), errors));
        }

        return eventDatas;
    }

    public Event getEvent(Container c, User u, int eventId, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo eventsTable = getTableInfo(schema, SNDSchema.EVENTS_TABLE_NAME);

        // Get from events table
        SimpleFilter eventFilter = new SimpleFilter(FieldKey.fromParts("EventId"), eventId, CompareType.EQUAL);
        TableSelector eventTs = new TableSelector(eventsTable, eventFilter, null);

        Event event = eventTs.getObject(Event.class);
        if (event != null)
        {
            TableInfo eventNotesTable = getTableInfo(schema, SNDSchema.EVENTNOTES_TABLE_NAME);

            // Get from eventNotes table
            SimpleFilter eventNotesFilter = new SimpleFilter(FieldKey.fromParts("EventId"), eventId, CompareType.EQUAL);
            Set<String> cols = new HashSet<>();
            cols.add("Note");
            TableSelector eventNoteTs = new TableSelector(eventNotesTable, cols, eventNotesFilter, null);

            event.setNote(eventNoteTs.getObject(String.class));
            event.setProjectIdRev(getProjectIdRev(c, u, event.getParentObjectId()));
            event.setEventData(getEventDatas(c, u, eventId, errors));
            addExtraFieldsToEvent(c, u, event, eventTs.getMap());
        }

        return event;
    }

    public boolean eventExists(Container c, User u, int eventId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.EVENTS_TABLE_NAME);
        sql.append(" WHERE EventId = ?");
        sql.add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.getRowCount() > 0;
    }

    private String getProjectObjectId(Container c, User u, String projectIdRev, BatchValidationException errors)
    {
        if (projectIdRev == null)
            errors.addRowError(new ValidationException("Invalid project id|rev."));

        String[] idRevParts = projectIdRev.split("\\|");

        if (idRevParts.length != 2)
            errors.addRowError(new ValidationException("Project Id|Rev not formatted correctly"));

        Integer projectId = Integer.parseInt(idRevParts[0]);
        Integer revisionNum = Integer.parseInt(idRevParts[1]);

        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ObjectId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(projectId).add(revisionNum);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        List<String> results = selector.getArrayList(String.class);
        if (results.size() < 1)
        {
            errors.addRowError(new ValidationException("Project|revision not found: " + projectIdRev ));
        }

        return results.size() > 0? results.get(0) : null;
    }

    @Nullable
    private String getProjectIdRev(Container c, User u, String objectId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId, RevisionNum FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.PROJECTS_TABLE_NAME);
        sql.append(" WHERE ObjectId = ?");
        sql.add(objectId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        String idRev = null;
        try(TableResultSet rs = selector.getResultSet())
        {
            for (Map<String, Object> row : rs)
            {
                idRev = row.get("ProjectId") + "|" + row.get("RevisionNum");
            }
        }
        catch (SQLException e)
        {
            // swallow
        }

        return idRev;
    }

    public void deleteEventNotes(Container c, User u, int eventId) throws SQLException, QueryUpdateServiceException, BatchValidationException, InvalidKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventNoteId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.EVENTNOTES_TABLE_NAME);
        sql.append(" WHERE EventId = ?");
        sql.add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        List<Integer> eventNoteIds = selector.getArrayList(Integer.class);

        QueryUpdateService eventNotesQus = getNewQueryUpdateService(schema, SNDSchema.EVENTNOTES_TABLE_NAME);

        Map<String, Object> row;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Integer eventNoteId : eventNoteIds)
        {
            row = new HashMap<>();
            row.put("EventNoteId", eventNoteId);
            rows.add(row);
        }

        eventNotesQus.deleteRows(u, c, rows, null, null);
    }

    public Event addExtraFieldsToEvent(Container c, User u, Event event, @Nullable Map<String, Object> row)
    {
        List<GWTPropertyDescriptor> extraFields = getExtraFields(c, u, SNDSchema.EVENTS_TABLE_NAME);
        Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
        for (GWTPropertyDescriptor extraField : extraFields)
        {
            if (row == null)
            {
                extras.put(extraField, "");
            }
            else
            {
                extras.put(extraField, row.get(extraField.getName()));
            }
        }
        event.setExtraFields(extras);

        return event;
    }

    private EventData addExtraFieldsToEventData(Container c, User u, EventData eventData, @Nullable Map<String, Object> row)
    {
        List<GWTPropertyDescriptor> extraFields = getExtraFields(c, u, SNDSchema.EVENTDATA_TABLE_NAME);
        Map<GWTPropertyDescriptor, Object> extras = new HashMap<>();
        for (GWTPropertyDescriptor extraField : extraFields)
        {
            if (row == null)
            {
                extras.put(extraField, "");
            }
            else
            {
                extras.put(extraField, row.get(extraField.getName()));
            }
        }
        eventData.setExtraFields(extras);

        return eventData;
    }

    public Event getEmptyEvent(Container c, User u)
    {
        Event event = new Event();
        event = addExtraFieldsToEvent(c, u, event, null);

        EventData eventData = new EventData();
        eventData = addExtraFieldsToEventData(c, u, eventData, null);

        List<EventData> eventDatas = new ArrayList<>();
        eventDatas.add(eventData);

        event.setEventData(eventDatas);

        return event;
    }

    private String generateLsid(Container c, String eventObjectId)
    {
        return new Lsid(Event.SND_EVENT_NAMESPACE, "Folder-" + c.getRowId(), eventObjectId).toString();
    }

    private String insertExpObjectProperties(Container c, EventData eventData) throws ValidationException
    {
        String eventObjectId = GUID.makeGUID();
        String objectURI = generateLsid(c, eventObjectId);

        OntologyManager.ensureObject(c, objectURI);

        ObjectProperty objectProperty;
        PropertyDescriptor propertyDescriptor;
        PropertyType propertyType;

        if (eventData.getAttributes() != null)
        {
            for (AttributeData attributeData : eventData.getAttributes())
            {
                propertyDescriptor = OntologyManager.getPropertyDescriptor(attributeData.getPropertyId());
                propertyType = PropertyType.getFromURI(propertyDescriptor.getConceptURI(), propertyDescriptor.getRangeURI());
                objectProperty = new ObjectProperty(objectURI, c, propertyDescriptor.getPropertyURI(), attributeData.getValue(), propertyType);
                OntologyManager.insertProperties(c, null, objectProperty);
            }
        }

        return objectURI;
    }

    private void getEventDataRows(Container c, EventData eventData, int eventId, List<Map<String, Object>> eventDataRows) throws ValidationException
    {
        String objectURI = insertExpObjectProperties(c, eventData);
        eventData.setObjectURI(objectURI);
        eventData.setEventId(eventId);

        if (eventData.getEventDataId() == null)
        {
            eventData.setEventDataId(SNDSequencer.EVENTDATAID.ensureId(c, null));
        }

        eventDataRows.add(eventData.getEventDataRow(c));

        if (eventData.getSubPackages() != null)
        {
            for (EventData data : eventData.getSubPackages())
            {
                data.setParentEventDataId(eventData.getEventDataId());
                getEventDataRows(c, data, eventId, eventDataRows);
            }
        }
    }

    private void insertEventDatas(Container c, User u, List<EventData> eventDatas, int eventId, BatchValidationException errors) throws ValidationException, SQLException, QueryUpdateServiceException, BatchValidationException, DuplicateKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        TableInfo eventDataTable = getTableInfo(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        QueryUpdateService eventDataQus = getNewQueryUpdateService(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        List<Map<String, Object>> eventDataRows = new ArrayList<>();

        for (EventData eventData : eventDatas)
        {
            getEventDataRows(c, eventData, eventId, eventDataRows);
        }

        try (DbScope.Transaction tx = eventDataTable.getSchema().getScope().ensureTransaction())
        {
            eventDataQus.insertRows(u, c, eventDataRows, errors, null, null);
            tx.commit();
        }
    }

    private void ensureSuperPkgsBelongToProject(Container c, User u, Event event, BatchValidationException errors)
    {
        if (event.getParentObjectId() == null)
        {
            errors.addRowError(new ValidationException("Project objectid is null."));
        }

        if (event.getEventData() != null && event.getEventData().size() > 0)
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);

            // Get from project items table
            SimpleFilter projectItemsFilter = new SimpleFilter(FieldKey.fromParts("ParentObjectId"), event.getParentObjectId(), CompareType.EQUAL);
            Set<String> cols = new TreeSet<>();
            cols.add("SuperPkgId");
            cols.add("Active");
            TableSelector projectItemsTs = new TableSelector(projectItemsTable, cols, projectItemsFilter, null);

            TableResultSet projectItems = projectItemsTs.getResultSet();
            boolean found;

            // Since event datas are in hierarchy structure, top level event datas are top level super packages
            for (EventData eventData : event.getEventData())
            {
                found = false;
                for (Map<String, Object> projectItem : projectItems)
                {
                    if ( (Integer)projectItem.get("SuperPkgId") == eventData.getSuperPkgId() && (Boolean)projectItem.get("Active"))
                    {
                        found = true;
                    }
                }

                if (!found)
                {
                    errors.addRowError(new ValidationException("Super package " + eventData.getSuperPkgId() + " is not allowed for this project revision."));
                }
            }
        }
    }

    private Package getPackageForSuperPackage(Container c, User u, int superPkgId, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT PkgId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_TABLE_NAME);
        sql.append(" WHERE SuperPkgId = ?");
        sql.add(superPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        List<Integer> pkgIds = Lists.newArrayList(selector.getObject(Integer.class));


        return getPackages(c, u, pkgIds, false, false,true, errors).get(0);
    }

    private void ensureValidPackage(EventData eventData, Package pkg, BatchValidationException errors)
    {
        List<AttributeData> attributes = eventData.getAttributes();
        Map<Integer, Boolean> incomingProps = Maps.newHashMap();
        boolean found;

        if (attributes.size() > 0)
        {
            for (AttributeData attribute : attributes)
            {
                incomingProps.put(attribute.getPropertyId(), false);
            }

            // iterate through defined properties for package
            for (GWTPropertyDescriptor gwtPropertyDescriptor : pkg.getAttributes())
            {
                found = false;

                // mark incoming properties that match expected
                for (AttributeData attribute : attributes)
                {
                    if (attribute.getPropertyId() == gwtPropertyDescriptor.getPropertyId())
                    {
                        found = true;
                        incomingProps.put(attribute.getPropertyId(), true);
                        break;
                    }
                }

                // verify required fields are found
                if (!found && gwtPropertyDescriptor.isRequired())
                {
                    errors.addRowError(new ValidationException("Required field " + gwtPropertyDescriptor.getName() + " in package " + pkg.getPkgId() + " not found."));
                }
            }

            // Verify all incoming properties were found in package
            for (Integer propId : incomingProps.keySet())
            {
                if (!incomingProps.get(propId))
                    errors.addRowError(new ValidationException("Field with property id " + propId + " is not part of package " + pkg.getPkgId()));
            }
        }

        for (SuperPackage superPackage : pkg.getSubpackages())
        {
            found = false;
            for (EventData data : eventData.getSubPackages())
            {
                if (data.getSuperPkgId() == superPackage.getSuperPkgId())
                {
                    found = true;
                    ensureValidPackage(data, superPackage.getPkg(), errors);
                }
            }

            if (!found && pkgContainsRequiredFields(superPackage.getPkg()))
                errors.addRowError(new ValidationException("Missing data for subpackage " + superPackage.getPkgId() + " which contains required fields"));
        }
    }

    private boolean pkgContainsRequiredFields(Package pkg)
    {
        for (GWTPropertyDescriptor gwtPropertyDescriptor : pkg.getAttributes())
        {
            if (gwtPropertyDescriptor.isRequired())
                return true;
        }

        return false;
    }

    private void ensureValidEventData(Container c, User u, Event event, BatchValidationException errors)
    {
        for (EventData eventData : event.getEventData())
        {
            ensureValidPackage(eventData, getPackageForSuperPackage(c, u, eventData.getSuperPkgId(), errors), errors);
        }
    }

    public void createEvent(Container c, User u, Event event, BatchValidationException errors)
    {
        String projectObjectId = getProjectObjectId(c, u, event.getProjectIdRev(), errors);
        event.setParentObjectId(projectObjectId);

        ensureSuperPkgsBelongToProject(c, u, event, errors);

        if (!errors.hasErrors())
        {
            ensureValidEventData(c, u, event, errors);

            if (!errors.hasErrors())
            {
                UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
                TableInfo eventTable = getTableInfo(schema, SNDSchema.EVENTS_TABLE_NAME);
                QueryUpdateService eventQus = getQueryUpdateService(eventTable);

                QueryUpdateService eventNotesQus = getNewQueryUpdateService(schema, SNDSchema.EVENTNOTES_TABLE_NAME);

                List<Map<String, Object>> eventRows = new ArrayList<>();
                eventRows.add(event.getEventRow(c));

                List<Map<String, Object>> eventNotesRows = new ArrayList<>();
                eventNotesRows.add(event.getEventNotesRow(c));

                try (DbScope.Transaction tx = eventTable.getSchema().getScope().ensureTransaction())
                {
                    eventQus.insertRows(u, c, eventRows, errors, null, null);
                    eventNotesQus.insertRows(u, c, eventNotesRows, errors, null, null);
                    insertEventDatas(c, u, event.getEventData(), event.getEventId(), errors);
                    NarrativeAuditProvider.addAuditEntry(c, u, "Fill in full narrative.", "Created event: " + event.getEventId());
                    tx.commit();
                }
                catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | ValidationException e)
                {
                    errors.addRowError(new ValidationException(e.getMessage()));
                }
            }
        }
    }

    public void deleteEventDatas(Container c, User u, int eventId) throws SQLException, QueryUpdateServiceException, BatchValidationException, InvalidKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventDataId FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.EVENTDATA_TABLE_NAME);
        sql.append(" WHERE EventId = ?");
        sql.add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        List<Integer> eventDataIds = selector.getArrayList(Integer.class);

        QueryUpdateService eventDataQus = getNewQueryUpdateService(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        Map<String, Object> row;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Integer eventDataId : eventDataIds)
        {
            row = new HashMap<>();
            row.put("EventDataId", eventDataId);
            rows.add(row);
        }

        eventDataQus.deleteRows(u, c, rows, null, null);
        deleteExpObjects(c, u, eventId);
    }

    private void deleteExpObjects(Container c, User u, int eventId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        TableInfo eventDataTable = getTableInfo(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        // Get from eventNotes table
        SimpleFilter eventDataFilter = new SimpleFilter(FieldKey.fromParts("EventId"), eventId, CompareType.EQUAL);
        Set<String> cols = new HashSet<>();
        cols.add("ObjectURI");
        TableSelector eventDataTs = new TableSelector(eventDataTable, cols, eventDataFilter, null);

        List<String> objectURIs = eventDataTs.getArrayList(String.class);

        for (String objectUri : objectURIs)
        {
            OntologyManager.deleteOntologyObjects(c, objectUri);
        }
    }

    public void updateEvent(Container c, User u, Event event, BatchValidationException errors)
    {
        String projectObjectId = getProjectObjectId(c, u, event.getProjectIdRev(), errors);
        event.setParentObjectId(projectObjectId);

        ensureSuperPkgsBelongToProject(c, u, event, errors);

        if (!errors.hasErrors())
        {
            ensureValidEventData(c, u, event, errors);

            if (!errors.hasErrors())
            {
                UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
                TableInfo eventTable = getTableInfo(schema, SNDSchema.EVENTS_TABLE_NAME);
                QueryUpdateService eventQus = getQueryUpdateService(eventTable);

                QueryUpdateService eventNotesQus = getNewQueryUpdateService(schema, SNDSchema.EVENTNOTES_TABLE_NAME);

                List<Map<String, Object>> eventRows = new ArrayList<>();
                eventRows.add(event.getEventRow(c));

                List<Map<String, Object>> eventNotesRows = new ArrayList<>();
                eventNotesRows.add(event.getEventNotesRow(c));

                try (DbScope.Transaction tx = eventTable.getSchema().getScope().ensureTransaction())
                {
                    eventQus.updateRows(u, c, eventRows, null, null, null);
                    deleteEventNotes(c, u, event.getEventId());
                    eventNotesQus.insertRows(u, c, eventNotesRows, errors, null, null);
                    deleteEventDatas(c, u, event.getEventId());
                    insertEventDatas(c, u, event.getEventData(), event.getEventId(), errors);
                    NarrativeAuditProvider.addAuditEntry(c, u, "Fill in full narrative.", "Updated event: " + event.getEventId());
                    tx.commit();
                }
                catch (QueryUpdateServiceException | BatchValidationException | SQLException | InvalidKeyException | DuplicateKeyException | ValidationException e)
                {
                    errors.addRowError(new ValidationException(e.getMessage()));
                }
            }
        }
    }
}