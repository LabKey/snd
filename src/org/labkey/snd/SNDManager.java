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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.cache.CacheManager;
import org.labkey.api.cache.StringKeyCache;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.DbSequence;
import org.labkey.api.data.DbSequenceManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.property.DomainUtil;
import org.labkey.api.gwt.client.model.GWTDomain;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
import org.labkey.api.snd.SNDDomainKind;
import org.labkey.api.snd.SuperPackage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SNDManager
{
    private static final SNDManager _instance = new SNDManager();
    private static final int _minPkgId = 10000;
    private static final String SND_DBSEQUENCE_NAME = "org.labkey.snd.api.Package";
    private final StringKeyCache<Object> _cache;

    private List<TableInfo> _attributeLookups = new ArrayList<>();

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

    public Integer generatePackageId(Container c)
    {
        DbSequence sequence = DbSequenceManager.get(c, SND_DBSEQUENCE_NAME);
        sequence.ensureMinimum(_minPkgId);
        return sequence.next();
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

    public void updatePackage(User u, Container c, Package pkg, BatchValidationException errors)
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
        if (!errors.hasErrors() && !((PackagesTable)pkgsTable).isPackageInUse(pkg.getPkgId()))
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), getPackageName(pkg.getPkgId()), c, u);

            GWTDomain<GWTPropertyDescriptor> updateDomain = new GWTDomain<>();
            updateDomain.setName(getPackageName(pkg.getPkgId()));
            updateDomain.setFields(pkg.getAttributes());
            updateDomain.setDomainURI(domainURI);

            PackageDomainKind kind = new PackageDomainKind();
            kind.updateDomain(c, u, updateDomain);
        }
    }

    public void createPackage(User u, Container c, Package pkg, BatchValidationException errors)
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

    public void createSuperPackages(User u, Container c, List<SuperPackage> superPkgs, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo superPkgsTable = getTableInfo(schema, SNDSchema.SUPERPKGS_TABLE_NAME);
        QueryUpdateService superPkgQus = getQueryUpdateService(superPkgsTable);

        // Flatten packages, combine children and parent in same list
        List<SuperPackage> flatSuperPackages = new ArrayList<>();
        for (SuperPackage parent : superPkgs)
        {
            flatSuperPackages.add(parent);
            for (SuperPackage child : parent.getChildPackages())
            {
                flatSuperPackages.add(child);
            }
        }
        
        List<Integer> savedSuperPkgs = getSavedSuperPkgs(c, u);
        List<Map<String, Object>> updates = new ArrayList<>();
        List<Map<String, Object>> inserts = new ArrayList<>();

        // Update existing rows and add new rows
        for (SuperPackage superPkg : flatSuperPackages)
        {
            superPkg.setSuperPkgPath(Integer.toString(superPkg.getSuperPkgId()));
            if (savedSuperPkgs.contains(superPkg.getSuperPkgId()))
            {
                updates.add(superPkg.getSuperPackageRow(c));
            }
            else
            {
                inserts.add(superPkg.getSuperPackageRow(c));
            }
        }

        try (DbScope.Transaction tx = superPkgsTable.getSchema().getScope().ensureTransaction())
        {
            superPkgQus.insertRows(u, c, inserts, errors, null, null);
            superPkgQus.updateRows(u, c, updates, null,null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException | InvalidKeyException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
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

    private List<GWTPropertyDescriptor> getPackageExtraFields(Container c, User u)
    {
        String uri = SNDDomainKind.getDomainURI(SNDSchema.NAME, SNDSchema.PKGS_TABLE_NAME, c, u);
        GWTDomain<GWTPropertyDescriptor> domain = DomainUtil.getDomainDescriptor(u, uri, c);
        if (domain != null)
            return domain.getFields();

        return Collections.emptyList();
    }

    public Package addExtraFieldsToPackage(Container c, User u, Package pkg, @Nullable Map<String, Object> row)
    {
        List<GWTPropertyDescriptor> extraFields = getPackageExtraFields(c, u);
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

    private int getTopLevelSuperPkgId(Container c, User u, int pkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT SuperPkgId FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" WHERE PkgId = ? AND ParentSuperPkgId IS NULL").add(pkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        if (selector.getArrayList(Integer.class).size() > 0)
            return selector.getArrayList(Integer.class).get(0);
        else
            return -1;
    }

    private List<SuperPackage> getChildSuperPkgs(Container c, User u, int superPkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        SQLFragment sql = new SQLFragment("SELECT sp.SuperPkgId, sp.PkgId, p.Description FROM ");
        sql.append(schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME), "sp");
        sql.append(" JOIN ");
        sql.append(schema.getTable(SNDSchema.PKGS_TABLE_NAME), "p");
        sql.append(" ON sp.PkgId = p.PkgId");
        sql.append(" WHERE ParentSuperPkgId = ?").add(superPkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);

        return selector.getArrayList(SuperPackage.class);
    }

    public Package addSubPackagesToPackage(Container c, User u, Package pkg)
    {
        int superPkgId = getTopLevelSuperPkgId(c, u, pkg.getPkgId());
        if (superPkgId > -1)
            pkg.setSubpackages(getChildSuperPkgs(c, u, superPkgId));

        return pkg;
    }

    public Package addLookupsToPkg(Container c, User u, Package pkg)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);
        Map<String, Map<String, Object>> sndLookups = ((SNDUserSchema)schema).getLookupSets();
        Map<String, String> lookups = new HashMap<>();

        String key, label;
        for (String sndLookup : sndLookups.keySet())
        {
            key = "snd." + sndLookup;
            label = ((String)sndLookups.get(sndLookup).get("Label"));
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

    private Package createPackage(Container c, User u, Map<String, Object> row)
    {
        Package pkg = new Package();
        if(row != null)
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
            addExtraFieldsToPackage(c, u, pkg, row);
            addSubPackagesToPackage(c, u, pkg);
            addLookupsToPkg(c, u, pkg);
        }

        return pkg;
    }

    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, BatchValidationException errors)
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
                packages.add(createPackage(c, u, row));
            }
        }

        return packages;
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
}