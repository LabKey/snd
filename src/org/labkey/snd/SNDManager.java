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
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.Results;
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
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.exp.property.ValidatorContext;
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
import org.labkey.api.query.ValidationError;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.settings.LookAndFeelProperties;
import org.labkey.api.snd.AttributeData;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventNarrativeOption;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
import org.labkey.api.snd.Project;
import org.labkey.api.snd.ProjectItem;
import org.labkey.api.snd.SNDDomainKind;
import org.labkey.api.snd.SNDSequencer;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.DateUtil;
import org.labkey.snd.query.PackagesTable;
import org.labkey.snd.table.PlainTextNarrativeDisplayColumn;
import org.labkey.snd.trigger.SNDTriggerManager;

import java.sql.ResultSet;
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
import java.util.TreeMap;
import java.util.TreeSet;

import static org.labkey.api.snd.EventNarrativeOption.HTML_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.REDACTED_HTML_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.REDACTED_TEXT_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.TEXT_NARRATIVE;

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

    /**
     * Deletes category package associations in junction table for a given package
     */
    public void deletePackageCategories(Container c, User u, int pkgId)
    {
        SQLFragment sql = new SQLFragment("DELETE FROM " + SNDSchema.getInstance().getTableInfoPkgCategoryJunction());
        sql.append(" WHERE PkgId = ? AND Container = ? ");
        sql.add(pkgId).add(c);

        SqlExecutor sqlex = new SqlExecutor(SNDSchema.getInstance().getSchema());
        sqlex.execute(sql);
    }

    /**
     * Generic get table info function
     */
    private TableInfo getTableInfo(@NotNull UserSchema schema, @NotNull String table)
    {
        TableInfo tableInfo = schema.getTable(table);
        if (tableInfo == null)
            throw new IllegalStateException(table + " TableInfo not found");

        return tableInfo;
    }

    /**
     * Generic get update service function
     */
    private QueryUpdateService getQueryUpdateService(@NotNull TableInfo table)
    {
        QueryUpdateService qus = table.getUpdateService();
        if (qus == null)
            throw new IllegalStateException(table.getName() + " query update service");

        return qus;
    }

    /**
     * Creates a new QUS for extensible tables. Used for tables that have had their QUS blocked.
     */
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

    /**
     * Gets lookup values for default values of a property descriptor. Used when creating json for a property descriptor.
     */
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

    /**
     * Gets key value of a default lookup value. Used when converting json to property descriptor.
     */
    public Object normalizeLookupDefaultValue(User u, Container c, String schema, String table, Object display)
    {
        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);
        if (userSchema == null)
            return null;

        TableInfo tableInfo = userSchema.getTable(table);
        if (table == null)
            return null;

        String pk;
        if (tableInfo.getPkColumnNames() != null)
        {
            pk = tableInfo.getPkColumnNames().get(0); // Only handling single value pks
        }
        else
        {
            return null;
        }

        SQLFragment sql = new SQLFragment("SELECT " + pk + " FROM ");
        sql.append(tableInfo, "l");
        sql.append(" WHERE " + tableInfo.getTitleColumn() + " = ?").add(display);
        SqlSelector selector = new SqlSelector(userSchema.getDbSchema(), sql);
        List<Object> lookupRows = selector.getArrayList(Object.class);

        if (lookupRows.size() < 1)
            return null;

        return lookupRows.get(0);
    }

    /**
     * Called from SNDService.savePackage when saving updates to an already existing package.
     */
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

    /**
     * Called from SNDService.savePackage when creating a new package.
     */
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

    /**
     * Gets all saved super package Ids
     */
    private List<Integer> getSavedSuperPkgs(Container c, User u)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "s");
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.getArrayList(Integer.class);
    }

    /**
     * Called from saveSuperPackages. Deletes any subpackages that have been removed from a super package.
     */
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

    /**
     * Called from create and update package functions, as well as import super packages functions.  Saves all the sub super packages
     * for a given package and the super package defined for the package itself.
     */
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

    /**
     * Gets the property descriptors for a given package by getting the full domain for the package
     */
    public List<GWTPropertyDescriptor> getPackageAttributes(Container c, User u, int pkgId)
    {
        String uri = PackageDomainKind.getDomainURI(SNDSchema.NAME, getPackageName(pkgId), c, u);
        GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, uri, c);
        if (domain != null)
            return domain.getFields();

        return Collections.emptyList();
    }

    /**
     * Gets the category ids associated with a given package
     */
    private Map<Integer, String> getPackageCategories(Container c, User u, int pkgId, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT cj.CategoryId, ca.Description FROM ");
        sql.append(schema.getTable(SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME), "cj");
        sql.append(" JOIN ");
        sql.append(schema.getTable(SNDSchema.PKGCATEGORIES__TABLE_NAME), "ca");
        sql.append(" ON cj.CategoryId = ca.CategoryId");
        sql.append(" WHERE PkgId = ?").add(pkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        Map<Integer, String> categories = new HashMap<>();
        try(TableResultSet rs = selector.getResultSet())
        {
            for (Map<String, Object> r : rs)
            {
                categories.put((Integer) r.get("CategoryId"), (String) r.get("Description"));
            }
        }
        catch (SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        return categories;
    }

    /**
     * Gets the extensible fields for a given table
     */
    private List<GWTPropertyDescriptor> getExtraFields(Container c, User u, String tableName)
    {
        String uri = SNDDomainKind.getDomainURI(SNDSchema.NAME, tableName, c, u);
        GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, uri, c);
        if (domain != null)
            return domain.getFields();

        return Collections.emptyList();
    }

    /**
     * Add extensible fields to the package object when getting the package for an API
     */
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

    /**
     * Recursive call to iterate through the hierarchy of a super package to get its subpackages
     */
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

    /**
     * Returns all super package IDs which correspond to this package ID
     */
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

    /**
     * Returns the top-level super package which corresponds to this package ID
     */
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

    /**
     * Returns the super packages which correspond to these super package IDs
     */
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

    /**
     * Convert all passed-in super packages to distinct top-level super packages
     */
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

    /**
     * Filters list of superPackageIds down to super packages which have no parent
     */
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

    /**
     * Only gets IDs for all child super packages that point to a certain super package ID
     */
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

    /**
     * Filters list of superPackageIds down to super packages that have parentSuperPackageId as a parent
     */
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

    /**
     * Return list of super package IDs that should be deleted based on passed-in superPackageIds and parentSuperPackage ID
     */
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

    /**
     * Used for building sql statements when adding subpackages to parameters
     */
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

    /**
     * Checks if package is descendent of a super package
     */
    public boolean isDescendent(Container c, User u, int topLevelSuperPkgId, int pkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT PkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE SuperPkgId = ?");
        sql.add(topLevelSuperPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        Integer topLevelPkgId = selector.getArrayList(Integer.class).get(0);

        sql = new SQLFragment("SELECT * FROM ");
        sql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_FUNCTION_NAME + "(?)");
        sql.append(" WHERE PkgId = ?");
        sql.add(topLevelPkgId).add(pkgId);

        selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.exists();
    }

    /**
     * Gets the full SuperPackage object for a given super package Id.  Option to include full subpackages as well,
     * otherwise just a list of super package Ids for subpackages.
     */
    @Nullable
    private SuperPackage getFullSuperPackage(Container c, User u, int superPkgId, boolean fullSubpackages, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.PkgId, sp.SortOrder, pkg.PkgId, pkg.Description, pkg.Active, pkg.Narrative, pkg.Repeatable FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" JOIN " + SNDSchema.NAME + "." + SNDSchema.PKGS_TABLE_NAME + " pkg");
        sql.append(" ON sp.PkgId = pkg.PkgId");
        sql.append(" WHERE sp.SuperPkgId = ?");
        sql.add(superPkgId);

        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        SuperPackage superPackage = selector.getObject(SuperPackage.class);

        if (fullSubpackages)
        {
            List<Integer> pkgIds = new ArrayList<>();
            pkgIds.add(superPackage.getPkgId());
            List<Package> pkgs = getPackages(c, u, pkgIds, true, true, true, errors);
            if (pkgs.size() > 0)
            {
                superPackage.setPkg(pkgs.get(0));
            }
        }

        if (superPackage != null)
            superPackage.setChildPackages(getAllChildSuperPkgs(c, u, superPackage.getPkgId(), fullSubpackages, errors));

        return superPackage;
    }

    /**
     * Recursively get all children for the super package which corresponds to pkgId
     */
    private List<SuperPackage> getAllChildSuperPkgs(Container c, User u, int pkgId, boolean includeFullSubpackages, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment childSql = new SQLFragment("SELECT * FROM ");
        childSql.append(SNDSchema.NAME + "." + SNDSchema.SUPERPKGS_FUNCTION_NAME + "(?)").add(pkgId);

        SqlSelector selector = new SqlSelector(schema.getDbSchema(), childSql);
        List<SuperPackage> descendants = selector.getArrayList(SuperPackage.class);
        List<SuperPackage> children = new ArrayList<>();

        SuperPackage root = null;
        Package childPkg;
        List<Integer> pkgIds;
        for (SuperPackage sPkg : descendants)
        {
            if (sPkg.getParentSuperPkgId() == null)
            {
                root = sPkg;
            }
            else if (includeFullSubpackages)
            {
                pkgIds = new ArrayList<>();
                pkgIds.add(sPkg.getPkgId());
                childPkg = getPackages(c, u, pkgIds, true, true, true, errors).get(0);
                sPkg.setPkg(childPkg);
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

    /**
     * Adds lookup sets to a package being retrieved in API
     */
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

    /**
     * Given a row from the snd.Pkgs table, this creates the Package object.  Options to include extensible columns, lookup values
     * and attributes of subpackages
     */
    private Package createPackage(Container c, User u, Map<String, Object> row, boolean includeExtraFields, boolean includeLookups, boolean includeFullSubpackages, BatchValidationException errors)
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
            pkg.setCategories(getPackageCategories(c, u, pkg.getPkgId(), errors));
            pkg.setAttributes(getPackageAttributes(c, u, pkg.getPkgId()));

            SuperPackage sPkg = getTopLevelSuperPkgForPkg(c, u, pkg.getPkgId());
            if (sPkg != null)
                pkg.setTopLevelSuperPkgId(sPkg.getSuperPkgId());

            pkg.setSubpackages(getAllChildSuperPkgs(c, u, pkg.getPkgId(), includeFullSubpackages, errors));
            if (includeExtraFields)
                addExtraFieldsToPackage(c, u, pkg, row);
            if (includeLookups)
                addLookupsToPkg(c, u, pkg);
        }

        return pkg;
    }

    /**
     * Gets a list of full packages given a list of package Ids. Options to include extensible columns, lookup values and
     * all attributes for sub packages.
     */
    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, boolean includeExtraFields, boolean includeLookups,
                                     boolean includeFullSubpackages, BatchValidationException errors)
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
                packages.add(createPackage(c, u, row, includeExtraFields, includeLookups, includeFullSubpackages, errors));
            }
        }

        return packages;
    }

    /**
     * Used for validation to ensure there are no later project revisions
     */
    public boolean projectRevisionIsLatest(Container c, User u, int id, int rev)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
        sql.append(" WHERE ProjectId = ? AND RevisionNum > ?");
        sql.add(id).add(rev);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return !selector.exists();
    }

    /**
     * Used for validation to ensure a project revision is valid
     */
    private boolean projectRevisionExists(Container c, User u, int id, int rev)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(id).add(rev);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.exists();
    }

    /**
     * Used for validation to check for overlap of dates in project with passed in row
     */
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

    /**
     * Used for validation to check reference Ids for overlap with other projects and not allow changing ref Id once
     * a project is in use.
     */
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

    /**
     * Used for validation.  Validates a project revision before allowing it to be saved.
     */
    private boolean isValidRevision(Container c, User u, Project project, boolean revision, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date endDate;

        SQLFragment sql = new SQLFragment("SELECT ProjectId, RevisionNum, StartDate, EndDate FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
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

    /**
     * Used for validation to check a valid reference Id and revision
     */
    private boolean validProject(Container c, User u, Project project, boolean revision, BatchValidationException errors)
    {
        if (revision && projectRevisionExists(c, u, project.getProjectId(), project.getRevisedRevNum()))
        {
            errors.addRowError(new ValidationException("Revision " + project.getRevisedRevNum() + " already exists for this project. Can only make revision from latest revision."));
        }

        isValidRevision(c, u, project, revision, errors);
        isValidReferenceId(c, u, project, revision, errors);

        return !errors.hasErrors();

    }

    /**
     * Called from SNDService.saveProject to create a new project.
     */
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

    /**
     * Used in reviseProject to update a project
     */
    private void updateProjectField(Container c, User u, int id, int rev, String field, String value)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("UPDATE " + SNDSchema.getInstance().getTableInfoProjects() );
        sql.append(" SET " + field + " = ?");
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(value).add(id).add(rev);

        new SqlExecutor(schema.getDbSchema().getScope()).execute(sql);
    }

    /**
     * Called from SNDService.saveProject to create a project revision.  This is called when an isRevision flag is passed
     * into SaveProject API.
     */
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
                sql.append(schema.getTable(SNDSchema.PROJECTITEMS_TABLE_NAME), "pi");
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

    /**
     * Called from SNDService.saveProject to update an existing project
     */
    public void updateProject(Container c, User u, Project project, BatchValidationException errors)
    {
        if (validProject(c, u, project, false, errors))
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

            // Erase all project items for this project
            SQLFragment sql = new SQLFragment("DELETE FROM " + SNDSchema.getInstance().getTableInfoProjectItems() );
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

    /**
     * Called from SNDService.saveProject to determine if a project exists.  If project exists then will perform an update
     * or revision.  If not, will create a new project.
     */
    public String getProjectObjectId(Container c, User u, Project project, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ObjectId FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
        sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
        sql.add(project.getProjectId()).add(project.getRevisionNum());
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        if (!selector.exists())
            return null;

        return selector.getArrayList(String.class).get(0);
    }

    /**
     * Gets project item Ids for a project.  Used when deleting a project.
     */
    public List<Map<String, Object>> getProjectItems(Container c, User u, int projectId, int revNum)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectItemId FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTITEMS_TABLE_NAME), "pi");
        sql.append(" JOIN ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
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

    /**
     * Gets extensible columns for a project. Used when getting a project in API call
     */
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

    /**
     * Returns all super package IDs which correspond to this package ID
     */
    @Nullable
    public static List<Integer> getProjectItemIdsForSuperPkgId(Container c, User u, Integer superPkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT pi.ProjectItemId FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTITEMS_TABLE_NAME), "pi");
        sql.append(" WHERE pi.SuperPkgId = ?").add(superPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.exists())
            return selector.getArrayList(Integer.class);
        else
            return null;
    }

    /**
     * Gets a project for GetProject API
     */
    public Project getProject(Container c, User u, int projectId, int revNum, BatchValidationException errors)
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
                superPackage = getFullSuperPackage(c, u, projectItem.getSuperPkgId(), false, errors);
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

    /**
     * Called from SNDService to allow lookup tables from outside the SND module to be added to the list of lookups
     * available package attributes.
     */
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

    /**
     * Gets registered attribute lookup sets
     */
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

    /**
     * Gets event data for a given event.  This includes the data from snd.EventData and the attribute data stored in
     * exp.ObjectProperty.  Recursively iterates through subpackages to get data.
     */
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
        Object propValue;
        for (GWTPropertyDescriptor gwtPropertyDescriptor : superPackage.getPkg().getAttributes())
        {
            attribute = new AttributeData();
            attribute.setPropertyName(gwtPropertyDescriptor.getName());
            attribute.setPropertyDescriptor(gwtPropertyDescriptor);
            attribute.setPropertyId(gwtPropertyDescriptor.getPropertyId());
            if (properties.get(gwtPropertyDescriptor.getPropertyURI()) != null)
            {
                propValue = properties.get(gwtPropertyDescriptor.getPropertyURI()).value();

                // Convert dates to ISO8601 format
                if (PropertyType.getFromURI(null, gwtPropertyDescriptor.getRangeURI()).equals(PropertyType.DATE))
                {
                    propValue = DateUtil.formatDateTime((Date)propValue, AttributeData.DATE_FORMAT);
                }
                else if (PropertyType.getFromURI(null, gwtPropertyDescriptor.getRangeURI()).equals(PropertyType.DATE_TIME))
                {
                    propValue = DateUtil.formatDateTime((Date)propValue, AttributeData.DATE_TIME_FORMAT);
                }

                attribute.setValue(propValue.toString());
            }

            attributeDatas.add(attribute);
        }

        eventData.setAttributes(attributeDatas);
        eventData.setNarrativeTemplate(superPackage.getNarrative());
        addExtraFieldsToEventData(c, u, eventData, ts.getMap());

        SQLFragment sql = new SQLFragment("SELECT EventDataId, SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.EVENTDATA_TABLE_NAME), "ed");
        sql.append(" WHERE ParentEventDataId = ?").add(eventDataId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        try(TableResultSet results = selector.getResultSet())
        {
            Integer superPkgId;
            SuperPackage eventDataSuperPkg = null;
            Map<Integer, EventData> subEventDatas = new TreeMap<>();  // preserve natural order of sort order keys
            Integer sortOrder = null;
            SuperPackage supPkg;

            List<SuperPackage> orderedChildPkgs = superPackage.getChildPackages();

            for (Map<String, Object> result : results)
            {
                superPkgId = (Integer) result.get("SuperPkgId");
                for (int i = 0; i < orderedChildPkgs.size(); i++)
                {
                    supPkg = orderedChildPkgs.get(i);
                    if (supPkg.getSuperPkgId().equals(superPkgId))
                    {
                        eventDataSuperPkg = supPkg;
                        sortOrder = supPkg.getSortOrder();

                        // If order not defined, then will order by eventdataid
                        if (sortOrder == null)
                        {
                            sortOrder = i;
                        }
                        break;
                    }
                }

                if (eventDataSuperPkg != null)
                {
                    subEventDatas.put(sortOrder, getEventData(c, u, (Integer) result.get("EventDataId"), eventDataSuperPkg, errors));
                }
                else
                {
                    errors.addRowError(new ValidationException("Super package not found for event data."));
                }
            }
            eventData.setSubPackages(new ArrayList<>(subEventDatas.values()));
        }
        catch (SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        return eventData;
    }

    /**
     * Gets event data for a given event.  This includes the data from snd.EventData and the attribute data stored in
     * exp.ObjectProperty.  Calls getEventData to get full hierarchy of event datas.
     */
    private List<EventData> getEventDatas(Container c, User u, int eventId, BatchValidationException errors)
    {
        List<EventData> eventDatas = new ArrayList<>();

        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT SuperPkgId, EventDataId FROM ");
        sql.append(schema.getTable(SNDSchema.EVENTDATA_TABLE_NAME), "ed");
        sql.append(" WHERE EventId = ? AND ParentEventDataId IS NULL").add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        Map<Integer, SuperPackage> topLevelSuperPackages = new HashMap<>();
        Integer superPkgId;

        try(TableResultSet results = selector.getResultSet())
        {
            for (Map<String, Object> result : results)
            {
                superPkgId = (Integer) result.get("SuperPkgId");
                if (!topLevelSuperPackages.containsKey(superPkgId))
                {
                    topLevelSuperPackages.put(superPkgId, getFullSuperPackage(c, u, superPkgId, true, errors));
                }

                eventDatas.add(getEventData(c, u, (Integer) result.get("EventDataId"), topLevelSuperPackages.get(superPkgId), errors));
            }
        }
        catch (SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }
        return eventDatas;
    }

    /**
     * Gets event for a given event Id.  Call from SNDService.getEvent
     */
    public Event getEvent(Container c, User u, int eventId, Set<EventNarrativeOption> narrativeOptions, boolean isPopulatingNarratives, BatchValidationException errors)
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
            Set<String> cols = new HashSet<>();
            cols.add("Note");
            TableSelector eventNoteTs = new TableSelector(eventNotesTable, cols, eventFilter, null);

            event.setNote(eventNoteTs.getObject(String.class));
            event.setProjectIdRev(getProjectIdRev(c, u, event.getParentObjectId(), errors));
            event.setEventData(getEventDatas(c, u, eventId, errors));
            addExtraFieldsToEvent(c, u, event, eventTs.getMap());

            // Get narrative from eventsCache table

            if (isPopulatingNarratives)
            {
                Map<EventNarrativeOption, String> narratives = getNarratives(c, u, narrativeOptions, event, errors);
                if (narratives != null)
                    event.setNarratives(narratives);
            }
        }

        return event;
    }

    private Map<EventNarrativeOption, String> getNarratives(Container c, User u, Set<EventNarrativeOption> narrativeOptions, Event event, BatchValidationException errors)
    {
        Map<EventNarrativeOption, String> narratives = null;
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        SimpleFilter eventFilter = new SimpleFilter(FieldKey.fromParts("EventId"), event.getEventId(), CompareType.EQUAL);

        if (narrativeOptions != null)
        {
            narratives = new HashMap<>();
            for (EventNarrativeOption narrativeOption : narrativeOptions)
            {
                switch (narrativeOption)
                {
                    case TEXT_NARRATIVE:
                        // Get text version from generateEventNarrative for better formatting (as opposed to using cache and PlainTextNarrativeDisplayColumn)
                        String textNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), false, false);
                        narratives.put(TEXT_NARRATIVE, textNarrative);
                        break;
                    case REDACTED_TEXT_NARRATIVE:
                        // Redacting means we have to generate on the fly, not from cache
                        String redactedTextNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), false, true);
                        narratives.put(REDACTED_TEXT_NARRATIVE, redactedTextNarrative);
                        break;
                    case HTML_NARRATIVE:
                        TableInfo eventsCacheTable = getTableInfo(schema, SNDSchema.EVENTSCACHE_TABLE_NAME);
                        TableSelector eventsCacheTs = new TableSelector(eventsCacheTable, eventFilter, null);
                        if (eventsCacheTs.exists())
                        {
                            try (Results eventsCacheResults = eventsCacheTs.getResults())
                            {
                                eventsCacheResults.next();
                                String htmlNarrative = eventsCacheResults.getString(FieldKey.fromParts("htmlNarrative"));
                                narratives.put(HTML_NARRATIVE, htmlNarrative);
                            }
                            catch (SQLException e)
                            {
                                errors.addRowError(new ValidationException(e.getMessage()));
                            }
                        }
                        else
                            errors.addRowError(new ValidationException("Event ID " + event.getEventId() + " exists but narrative unexpectedly not found in EventsCache."));

                        break;
                    case REDACTED_HTML_NARRATIVE:
                        // Redacting means we have to generate on the fly, not from cache
                        String redactedHtmlNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), true, true);
                        narratives.put(REDACTED_HTML_NARRATIVE, redactedHtmlNarrative);
                        break;
                }
            }
        }

        return narratives;
    }

    /**
     * Called from SNDService.saveEvent to determine if save event is creating a new event or updating an event.
     */
    public boolean eventExists(Container c, User u, int eventId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventId FROM ");
        sql.append(schema.getTable(SNDSchema.EVENTS_TABLE_NAME), "ev");
        sql.append(" WHERE EventId = ?");
        sql.add(eventId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.exists();
    }

    /**
     * Get a project ObjectId given a projectId and revision in the format projectId|rev (ex. 61|1).
     */
    private String getProjectObjectId(Container c, User u, Event event)
    {
        if (event.getProjectIdRev() == null)
        {
            event.setEventException(new ValidationException("Invalid project id|rev."));
        }
        else
        {

            String[] idRevParts = event.getProjectIdRev().split("\\|");

            if (idRevParts.length != 2)
            {
                event.setEventException(new ValidationException("Project Id|Rev not formatted correctly"));
            }
            else
            {
                Integer projectId = null;
                Integer revisionNum = null;
                try
                {
                    projectId = Integer.parseInt(idRevParts[0]);
                    revisionNum = Integer.parseInt(idRevParts[1]);
                }
                catch (NumberFormatException e)
                {
                    event.setEventException(new ValidationException("Number Format Exception on projectIdRev: " + e.getMessage()));
                }

                if (!event.hasErrors() && projectId != null && revisionNum != null)
                {
                    UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

                    SQLFragment sql = new SQLFragment("SELECT ObjectId FROM ");
                    sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
                    sql.append(" WHERE ProjectId = ? AND RevisionNum = ?");
                    sql.add(projectId).add(revisionNum);
                    SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

                    List<String> results = selector.getArrayList(String.class);
                    if (results.size() < 1)
                    {
                        event.setEventException(new ValidationException("Project|revision not found: " + event.getProjectIdRev()));
                    }

                    return results.size() > 0 ? results.get(0) : null;
                }
            }
        }

        return null;
    }

    /**
     * Get a projectId and revision in the format projectId|rev (ex. 61|1). Used for getEvent.
     */
    @Nullable
    private String getProjectIdRev(Container c, User u, String objectId, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT ProjectId, RevisionNum FROM ");
        sql.append(schema.getTable(SNDSchema.PROJECTS_TABLE_NAME), "pr");
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
            errors.addRowError(new ValidationException(e.getMessage()));
        }

        return idRev;
    }

    /**
     * Delete event notes for a given event id.
     */
    public void deleteEventNotes(Container c, User u, int eventId) throws SQLException, QueryUpdateServiceException, BatchValidationException, InvalidKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventNoteId FROM ");
        sql.append(schema.getTable(SNDSchema.EVENTNOTES_TABLE_NAME), "en");
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

    /**
     * Add extensible columns to an event.
     */
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

    /**
     * Add extensible columns to event data.
     */
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

    /**
     * Get an empty event with just event meta data.  Used for creating the initial forms.  Called from SND GetEvent API
     */
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

    /**
     * Create ObjectURI that will link snd.EventData rows with exp.Object rows
     */
    public String generateLsid(Container c, String eventDataId)
    {
        return new Lsid(Event.SND_EVENT_NAMESPACE, "Folder-" + c.getRowId(), eventDataId).toString();
    }

    /**
     * Gets package Id for a given super package
     */
    public Integer getPackageIdForSuperPackage(Container c, User u, int superPkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT PkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE SuperPkgId = ?");
        sql.add(superPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.getObject(Integer.class);
    }

    private List<ValidationException> validateProperty(Container c, User u, PropertyDescriptor pd, ObjectProperty op)
    {
        List<ValidationError> errors = new ArrayList<>();
        ValidatorContext validatorCache = new ValidatorContext(c, u);
        OntologyManager.validateProperty(PropertyService.get().getPropertyValidators(pd), pd, op, errors, validatorCache);

        List<ValidationException> exceptions = new ArrayList<>();
        for (ValidationError error : errors)
        {
            exceptions.add(new ValidationException(error.getMessage()));
        }

        return exceptions;
    }

    /**
     * Inserts event data and attribute data into exp.Object and exp.ObjectProperty tables.  Used in save event API
     */
    private String insertExpObjectProperties(Container c, User u, Event event, EventData eventData) throws ValidationException
    {
//        String eventObjectId = GUID.makeGUID();
        if (eventData == null || eventData.getEventDataId() == null)
        {
            event.setEventException(new ValidationException("Cannot enter exp object for null event data."));
        }

        String objectURI = generateLsid(c, Integer.toString(eventData.getEventDataId()));
        Integer pkgId = getPackageIdForSuperPackage(c, u, eventData.getSuperPkgId());

        OntologyManager.ensureObject(c, objectURI);

        ObjectProperty objectProperty;
        PropertyDescriptor propertyDescriptor;
        PropertyType propertyType;

        for (AttributeData attributeData : eventData.getAttributes())
        {
            if (attributeData.getPropertyName() != null)
            {
                propertyDescriptor = OntologyManager.getPropertyDescriptor(PackageDomainKind.getDomainURI(
                        SNDSchema.NAME, PackageDomainKind.getPackageKindName(), c, u) + "-" + pkgId + "#" + attributeData.getPropertyName(), c);
            }
            else
            {
                propertyDescriptor = OntologyManager.getPropertyDescriptor(attributeData.getPropertyId());
            }

            if (propertyDescriptor != null)
            {
                propertyType = PropertyType.getFromURI(propertyDescriptor.getConceptURI(), propertyDescriptor.getRangeURI());
                objectProperty = new ObjectProperty(objectURI, c, propertyDescriptor.getPropertyURI(), attributeData.getValue(), propertyType);

                // Validate first to catch validation errors.  Relying on exception thrown in insertProperties creates issues
                // with nested transactions containing a lock.
                List<ValidationException> validationExceptions = validateProperty(c, u, propertyDescriptor, objectProperty);
                if (validationExceptions.size() > 0)
                {
                    attributeData.setException(event, validationExceptions.get(0)); // just handling on exception
                }
                else
                {
                    OntologyManager.insertProperties(c, null, objectProperty);
                }
            }
        }

        return objectURI;
    }

    /**
     * Recursive call that iterates through event data and its sub packages to insert event data and attribute data.
     */
    private void getEventDataRows(Container c, User u, Event event, EventData eventData, List<Map<String, Object>> eventDataRows) throws ValidationException
    {
        if (eventData.getEventDataId() == null)
        {
            eventData.setEventDataId(SNDSequencer.EVENTDATAID.ensureId(c, null));
        }

        String objectURI = insertExpObjectProperties(c, u, event, eventData);
        eventData.setObjectURI(objectURI);
        if (event.getEventId() != null)
            eventData.setEventId(event.getEventId());

        eventDataRows.add(eventData.getEventDataRow(c));

        if (eventData.getSubPackages() != null)
        {
            for (EventData data : eventData.getSubPackages())
            {
                data.setParentEventDataId(eventData.getEventDataId());
                getEventDataRows(c, u, event, data, eventDataRows);
            }
        }
    }

    /**
     * Inserts event and attribute data and their sub packages.
     */
    private void insertEventDatas(Container c, User u, Event event, BatchValidationException errors) throws ValidationException, SQLException, QueryUpdateServiceException, BatchValidationException, DuplicateKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        TableInfo eventDataTable = getTableInfo(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        QueryUpdateService eventDataQus = getNewQueryUpdateService(schema, SNDSchema.EVENTDATA_TABLE_NAME);

        List<Map<String, Object>> eventDataRows = new ArrayList<>();

        if (event.getEventData() != null)
        {
            for (EventData eventData : event.getEventData())
            {
                getEventDataRows(c, u, event, eventData, eventDataRows);
            }

            try (DbScope.Transaction tx = eventDataTable.getSchema().getScope().ensureTransaction())
            {
                eventDataQus.insertRows(u, c, eventDataRows, errors, null, null);
                tx.commit();
            }
        }
    }

    /**
     * Used to validate save event API call.  Verifies super packages belong to a project.
     */
    private void ensureSuperPkgsBelongToProject(Container c, User u, Event event)
    {
        if (event.getParentObjectId() == null)
        {
            event.setEventException(new ValidationException("Project is not found."));
        }

        if (!event.hasErrors() && event.getEventData() != null && event.getEventData().size() > 0)
        {
            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);

            // Get from project items table
            SimpleFilter projectItemsFilter = new SimpleFilter(FieldKey.fromParts("ParentObjectId"), event.getParentObjectId(), CompareType.EQUAL);
            Set<String> cols = new TreeSet<>();
            cols.add("SuperPkgId");
            cols.add("Active");
            TableSelector projectItemsTs = new TableSelector(projectItemsTable, cols, projectItemsFilter, null);

            try(TableResultSet projectItems = projectItemsTs.getResultSet())
            {
                boolean found;

                // Since event datas are in hierarchy structure, top level event datas are top level super packages
                for (EventData eventData : event.getEventData())
                {
                    found = false;
                    for (Map<String, Object> projectItem : projectItems)
                    {
                        if ((Integer) projectItem.get("SuperPkgId") == eventData.getSuperPkgId() && (Boolean) projectItem.get("Active"))
                        {
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        event.setEventException(new ValidationException("Super package " + eventData.getSuperPkgId() + " is not allowed for this project revision."));
                    }
                }
            }
            catch (SQLException e)
            {
                event.setEventException(new ValidationException(e.getMessage()));
            }
        }
    }

    /**
     * Gets the full package for a given super pakcage Id.  Uses the SNDManager.getPackages function used in SND getPackage API
     */
    private Package getPackageForSuperPackage(Container c, User u, int superPkgId, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT PkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE SuperPkgId = ?");
        sql.add(superPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        List<Integer> pkgIds = Lists.newArrayList(selector.getObject(Integer.class));


        return getPackages(c, u, pkgIds, false, false,true, errors).get(0);
    }

    /**
     * Ensure incoming event data super packages match the structure of the top level super package and that required fields
     * are filled in.
     */
    private void ensureValidPackage(Event event, EventData eventData, Package pkg)
    {
        List<AttributeData> attributes = eventData.getAttributes();
        Map<AttributeData, Boolean> incomingProps = Maps.newHashMap();
        boolean found;

        if (attributes.size() > 0)
        {
            for (AttributeData attribute : attributes)
            {
                incomingProps.put(attribute, false);
            }

            // iterate through defined properties for package
            for (GWTPropertyDescriptor gwtPropertyDescriptor : pkg.getAttributes())
            {
                found = false;

                // mark incoming properties that match expected
                for (AttributeData attribute : attributes)
                {
                    if ((attribute.getPropertyId() == gwtPropertyDescriptor.getPropertyId()) ||
                            (gwtPropertyDescriptor.getName().equals(attribute.getPropertyName())))
                    {
                        found = true;
                        incomingProps.put(attribute, true);
                        attribute.setPropertyDescriptor(gwtPropertyDescriptor);
                        attribute.setPropertyName(gwtPropertyDescriptor.getName());
                        attribute.setPropertyId(gwtPropertyDescriptor.getPropertyId());
                        break;
                    }
                }

                // verify required fields are found
                if (!found && gwtPropertyDescriptor.isRequired())
                {
                    eventData.setException(event, new ValidationException("Required field '" + gwtPropertyDescriptor.getName() + "' in package " + pkg.getPkgId() + " not found.",
                            gwtPropertyDescriptor.getName(), ValidationException.SEVERITY.ERROR));
                }
            }

            // Verify all incoming properties were found in package
            for (AttributeData prop : incomingProps.keySet())
            {
                if (!incomingProps.get(prop))
                    prop.setException(event, new ValidationException("Property " + prop.getPropertyId() + " is not part of package " + pkg.getPkgId(), ValidationException.SEVERITY.ERROR));
            }
        }

        // Validate subpackages
        for (SuperPackage superPackage : pkg.getSubpackages())
        {
//            found = false;
            for (EventData data : eventData.getSubPackages())
            {
                if (data.getSuperPkgId() == superPackage.getSuperPkgId())
                {
//                    found = true;
                    ensureValidPackage(event, data, superPackage.getPkg());
                }
            }

            //TODO: Change this to ensure all required subpackages are found. Use eventData.setException if required pkgs missing.
//            if (!found && pkgContainsRequiredFields(superPackage.getPkg()))
//                errors.addRowError(new ValidationException("Missing data for subpackage " + superPackage.getPkgId() + " which contains required fields"));
        }
    }

    /**
     * Helper function to check if a package contains required fields.  Used to determine if missing packages in event
     * data has required fields.
     */
    private boolean pkgContainsRequiredFields(Package pkg)
    {
        for (GWTPropertyDescriptor gwtPropertyDescriptor : pkg.getAttributes())
        {
            if (gwtPropertyDescriptor.isRequired())
                return true;
        }

        return false;
    }

    /**
     * Iterates through top level super package event datas to validate data.
     */
    private void ensureValidEventData(Container c, User u, Event event)
    {
        BatchValidationException errors = new BatchValidationException();

        for (EventData eventData : event.getEventData())
        {
            ensureValidPackage(event, eventData, getPackageForSuperPackage(c, u, eventData.getSuperPkgId(), errors));
        }

        if (errors.hasErrors())
        {
            event.addBatchValidationExceptions(errors);
        }
    }

    /**
     * Called from SNDService.saveEvent to insert a new event.
     */
    public Event createEvent(Container c, User u, Event event, boolean validateOnly)
    {
        List<SuperPackage> topLevelPkgs = getTopLevelSuperPkgs(c, u, event);

        SNDTriggerManager.get().fireInsertTriggers(c, u, event, topLevelPkgs);

        if (!event.hasErrors())
        {
            String projectObjectId = getProjectObjectId(c, u, event);

            if (!event.hasErrors())
            {
                event.setParentObjectId(projectObjectId);

                if (!event.hasErrors())
                {
                    ensureSuperPkgsBelongToProject(c, u, event);

                    if (!event.hasErrors())
                    {
                        ensureValidEventData(c, u, event);

                        if (!event.hasErrors() && !validateOnly)
                        {
                            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
                            TableInfo eventTable = getTableInfo(schema, SNDSchema.EVENTS_TABLE_NAME);
                            QueryUpdateService eventQus = getQueryUpdateService(eventTable);
                            QueryUpdateService eventNotesQus = getNewQueryUpdateService(schema, SNDSchema.EVENTNOTES_TABLE_NAME);
                            QueryUpdateService eventsCacheQus = getNewQueryUpdateService(schema, SNDSchema.EVENTSCACHE_TABLE_NAME);

                            String htmlEventNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), true, false);
                            Map<String, Object> eventsCacheRow = getEventsCacheRow(c, u, event.getEventId(), htmlEventNarrative);
                            String textEventNarrative = PlainTextNarrativeDisplayColumn.removeHtmlTagsFromNarrative(htmlEventNarrative);
                            BatchValidationException errors = new BatchValidationException();

                            try (DbScope.Transaction tx = eventTable.getSchema().getScope().ensureTransaction())
                            {
                                eventQus.insertRows(u, c, Collections.singletonList(event.getEventRow(c)), errors, null, null);
                                eventNotesQus.insertRows(u, c, Collections.singletonList(event.getEventNotesRow(c)), errors, null, null);
                                insertEventDatas(c, u, event, errors);
                                eventsCacheQus.insertRows(u, c, Collections.singletonList(eventsCacheRow), errors, null, null);
                                generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), true, false);
                                NarrativeAuditProvider.addAuditEntry(c, u, event.getEventId(), event.getSubjectId(), event.getDate(), textEventNarrative, "Create event");
                                tx.commit();
                            }
                            catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | ValidationException e)
                            {
                                event.setEventException(new ValidationException(e.getMessage(), ValidationException.SEVERITY.ERROR));
                            }
                            finally
                            {
                                if (errors.hasErrors())
                                {
                                    event.addBatchValidationExceptions(errors);
                                }
                            }
                        }
                    }
                }
            }
        }
        return event;
    }

    private Map<String, Object> getEventsCacheRow(Container c, User u, int eventId, String htmlNarrative)
    {
        Map<String, Object> eventsCacheRow = new CaseInsensitiveHashMap<>();
        eventsCacheRow.put("EventId", eventId);
        eventsCacheRow.put("HtmlNarrative", htmlNarrative);
        eventsCacheRow.put("Container", c.getId());
        return eventsCacheRow;
    }

    /**
     * Deletes cached narrative for a given event
     */
    public void deleteEventsCache(Container c, User u, int eventId) throws SQLException, QueryUpdateServiceException, BatchValidationException, InvalidKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        QueryUpdateService eventsCacheQus = getNewQueryUpdateService(schema, SNDSchema.EVENTSCACHE_TABLE_NAME);

        Map<String, Object> row = new HashMap<>();
        row.put("EventId", eventId);

        eventsCacheQus.deleteRows(u, c, Collections.singletonList(row), null, null);
    }

    /**
     * Deletes and event datas associated with an eventId and their associated exp.Object and attribute data in exp.ObjectProperty
     */
    public void deleteEventDatas(Container c, User u, int eventId) throws SQLException, QueryUpdateServiceException, BatchValidationException, InvalidKeyException
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT EventDataId FROM ");
        sql.append(schema.getTable(SNDSchema.EVENTDATA_TABLE_NAME), "ed");
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

    /**
     * Deletes exp Objects and ObjectProperties for a given event
     */
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

    /**
     * Called from SNDService.saveEvent to update an existing event.
     */
    public Event updateEvent(Container c, User u, Event event, boolean validateOnly)
    {
        List<SuperPackage> topLevelPkgs = getTopLevelSuperPkgs(c, u, event);

        SNDTriggerManager.get().fireUpdateTriggers(c, u, event, topLevelPkgs);

        if (!event.hasErrors())
        {
            String projectObjectId = getProjectObjectId(c, u, event);

            if (!event.hasErrors())
            {
                event.setParentObjectId(projectObjectId);

                if (!event.hasErrors())
                {
                    ensureSuperPkgsBelongToProject(c, u, event);

                    if (!event.hasErrors())
                    {
                        ensureValidEventData(c, u, event);

                        if (!event.hasErrors() && !validateOnly)
                        {
                            UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
                            TableInfo eventTable = getTableInfo(schema, SNDSchema.EVENTS_TABLE_NAME);
                            QueryUpdateService eventQus = getQueryUpdateService(eventTable);
                            QueryUpdateService eventNotesQus = getNewQueryUpdateService(schema, SNDSchema.EVENTNOTES_TABLE_NAME);
                            QueryUpdateService eventsCacheQus = getNewQueryUpdateService(schema, SNDSchema.EVENTSCACHE_TABLE_NAME);

                            String htmlEventNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), true, false);
                            Map<String, Object> eventsCacheRow = getEventsCacheRow(c, u, event.getEventId(), htmlEventNarrative);
                            String textEventNarrative = PlainTextNarrativeDisplayColumn.removeHtmlTagsFromNarrative(htmlEventNarrative);
                            BatchValidationException errors = new BatchValidationException();

                            try (DbScope.Transaction tx = eventTable.getSchema().getScope().ensureTransaction())
                            {
                                eventQus.updateRows(u, c, Collections.singletonList(event.getEventRow(c)), null, null, null);
                                deleteEventNotes(c, u, event.getEventId());
                                eventNotesQus.insertRows(u, c, Collections.singletonList(event.getEventNotesRow(c)), errors, null, null);
                                deleteEventDatas(c, u, event.getEventId());
                                insertEventDatas(c, u, event, errors);
                                eventsCacheQus.updateRows(u, c, Collections.singletonList(eventsCacheRow), null, null, null);
                                NarrativeAuditProvider.addAuditEntry(c, u, event.getEventId(), event.getSubjectId(), event.getDate(), textEventNarrative, "Event update");
                                tx.commit();
                            }
                            catch (QueryUpdateServiceException | BatchValidationException | SQLException | InvalidKeyException | DuplicateKeyException | ValidationException e)
                            {
                                event.setEventException(new ValidationException(e.getMessage(), ValidationException.SEVERITY.ERROR));
                            }
                            finally
                            {
                                if (errors.hasErrors())
                                {
                                    event.addBatchValidationExceptions(errors);
                                }
                            }
                        }
                    }
                }
            }
        }
        return event;
    }

    /**
     * Called from SNDController.RefreshNarrativeCacheAction to truncate and repopulate the event narrative cache.
     */
    public boolean refreshNarrativeCache(Container c, User u) throws BatchValidationException, SQLException, QueryUpdateServiceException, DuplicateKeyException
    {
        BatchValidationException errors = new BatchValidationException();

        UserSchema sndSchema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        QueryUpdateService eventsCacheQus = getNewQueryUpdateService(sndSchema, SNDSchema.EVENTSCACHE_TABLE_NAME);

        eventsCacheQus.truncateRows(u, c, null, null);

        TableInfo eventsTableInfo = sndSchema.getTable(SNDSchema.EVENTS_TABLE_NAME);
        ColumnInfo eventIdColumnInfo = eventsTableInfo.getColumn("EventId");

        List<Integer> eventIds = new ArrayList<>();

        try (ResultSet rs = QueryService.get().select(eventsTableInfo, Collections.singletonList(eventIdColumnInfo), null, null))
        {
            while (rs.next())
            {
                eventIds.add(rs.getInt("eventId"));
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();

        for (int eventId : eventIds)
        {
            Event event = getEvent(c, u, eventId, null, false, errors);  // don't populate narratives or set narrative options, since we're repopulating the narrative cache
            String eventNarrative = generateEventNarrative(c, event, getTopLevelSuperPkgs(c, u, event), true, false);
            Map<String, Object> row = new CaseInsensitiveHashMap<>();
            row.put("EventId", eventId);
            row.put("HtmlNarrative", eventNarrative);
            row.put("Container", c);
            rows.add(row);
        }

        eventsCacheQus.insertRows(u, c, rows, errors, null, null);

        return !errors.hasErrors();
    }

    /**
     * Returns a list of full top level super packages for the passed in event
     */
    private List<SuperPackage> getTopLevelSuperPkgs(Container c, User u, Event event)
    {
        List<SuperPackage> topLevelPkgs = new ArrayList<>();
        BatchValidationException errors = new BatchValidationException();

        if (event.getEventData() != null)
        {
            for (EventData eventData : event.getEventData())
            {
                topLevelPkgs.add(getFullSuperPackage(c, u, eventData.getSuperPkgId(), true, errors));
            }
        }

        if (errors.hasErrors())
        {
            event.addBatchValidationExceptions(errors);
        }

        return topLevelPkgs;
    }

    /**
     * Finds super package with matching superPkgId
     */
    public SuperPackage getSuperPackage(int superPkgId, List<SuperPackage> superPkgs)
    {
        for (SuperPackage superPkg : superPkgs)
        {
            if (superPkg.getSuperPkgId() != null && superPkg.getSuperPkgId() == superPkgId)
            {
                return superPkg;
            }
        }

        return null;
    }

    private String handleNarrativeDate(Container c, Event event, AttributeData attributeData, String value, String format, boolean dateTime)
    {
        String result = "Undefined Date";
        Date date = null;
        try
        {
            date = DateUtil.parseDateTime(value, format);
        }
        catch (ParseException e)
        {
            attributeData.setException(event, new ValidationException(attributeData.getPropertyName() + ": " + e.getMessage()
                    , attributeData.getPropertyName(), ValidationException.SEVERITY.ERROR));
        }

        if (date != null)
        {
            if (dateTime)
            {
                result = DateUtil.formatDateTime(date, LookAndFeelProperties.getInstance(c).getDefaultDateTimeFormat());
            }
            else
            {
                result = DateUtil.formatDateTime(date, LookAndFeelProperties.getInstance(c).getDefaultDateFormat());
            }
        }

        return result;
    }

    /**
     * Recursive function building up the narrative.  Iterates through the event datas filling in the tokens in the template
     * with real values.  Formats based on html or plain text and creates redacted or non-redacted version. Called from
     * generateEventNarrative.
     */
    private String generateEventDataNarrative(Container c, Event event, EventData eventData, SuperPackage superPackage, int tabIndex, boolean genHtml, boolean genRedacted)
    {
        StringBuilder eventDataNarrative = new StringBuilder();
        if (superPackage.getNarrative() != null)
        {
            eventDataNarrative.append(superPackage.getNarrative());
        }

        if (genHtml)
        {
            eventDataNarrative.insert(0, "<div class='" + EventData.EVENT_DATA_CSS_CLASS + "'>");
        }
        else
        {
            // plain text indenting
            StringBuilder tabs = new StringBuilder("\n");
            for (int t = 0; t<tabIndex; t++)
            {
                tabs.append("\t");
            }

            eventDataNarrative.insert(0, tabs);
        }

        if (superPackage.getPkg() != null)
        {
            List<GWTPropertyDescriptor> properties = superPackage.getPkg().getAttributes();
            Map<String, GWTPropertyDescriptor> propsByName = new HashMap<>();
            Map<Integer, GWTPropertyDescriptor> propsById = new HashMap<>();
            for (GWTPropertyDescriptor p : properties)
            {
                propsByName.put(p.getName(), p);
                propsById.put(p.getPropertyId(), p);
            }

            GWTPropertyDescriptor pd;
            String value;
            for (AttributeData attributeData : eventData.getAttributes())
            {
                pd = propsByName.get(attributeData.getPropertyName());
                value = null;

                if (pd == null)
                {
                    pd = propsById.get(attributeData.getPropertyId());
                }

                if (pd == null)
                    continue;

                if (genRedacted)
                {
                    value = pd.getRedactedText();
                }

                if (value == null)
                {
                    value = attributeData.getValue();
                }

                if (PropertyType.getFromURI(null, pd.getRangeURI()) == PropertyType.DATE_TIME)
                {
                    value = handleNarrativeDate(c, event, attributeData, value, AttributeData.DATE_TIME_FORMAT, true);
                }

                if (PropertyType.getFromURI(null, pd.getRangeURI()) == PropertyType.DATE)
                {
                    value = handleNarrativeDate(c, event, attributeData, value, AttributeData.DATE_FORMAT, false);
                }

                if (value != null)
                {
                    if (genHtml)
                        value = "<span class='" + AttributeData.ATTRIBUTE_DATA_CSS_CLASS + "'>" + value + "</span>";

                    eventDataNarrative = new StringBuilder(eventDataNarrative.toString().replace("{" + pd.getName() + "}", value));
                }
            }

            if (eventData.getSubPackages() != null)
            {
                tabIndex++;
                for (EventData data : eventData.getSubPackages())
                {
                    eventDataNarrative.append(generateEventDataNarrative(c, event, data, getSuperPackage(data.getSuperPkgId(), superPackage.getChildPackages()), tabIndex, genHtml, genRedacted));
                }
            }
        }

        if (genHtml)
        {
            eventDataNarrative.append("</div>\n");
        }

        return eventDataNarrative.toString();
    }

    /**
     * Call this function to generate the event narrative.  Options to generate in html or plain text and redacted or
     * non-redacted version.
     */
    private String generateEventNarrative(Container c, Event event, List<SuperPackage> superPkgs, boolean genHtml, boolean genRedacted)
    {
        StringBuilder narrative = new StringBuilder();
        if (event.getDate() != null)
        {
            String dateValue = DateUtil.formatDateTime(event.getDate(), LookAndFeelProperties.getInstance(c).getDefaultDateTimeFormat());

            if (genHtml)
            {
                narrative.append("<div class='" + Event.SND_EVENT_DATE_CSS_CLASS + "'>").append(dateValue).append("</div>\n");
            }
            else
            {
                narrative.append(dateValue).append("\n");
            }
        }

        if (event.getSubjectId() != null)
        {
            if (genHtml)
            {
                narrative.append("<div class='" + Event.SND_EVENT_SUBJECT_CSS_CLASS + "'>Subject Id: ").append(event.getSubjectId()).append("</div>\n");
            }
            else
            {
                narrative.append("Subject Id: ").append(event.getSubjectId()).append("\n");
            }
        }

        if (genHtml)
        {
            narrative.append("<br>");
        }

        if (event.getEventData() != null)
        {
            for (EventData eventData : event.getEventData())
            {
                narrative.append(generateEventDataNarrative(c, event, eventData, getSuperPackage(eventData.getSuperPkgId(), superPkgs), 0, genHtml, genRedacted));

            }
        }

        return narrative.toString();
    }
}