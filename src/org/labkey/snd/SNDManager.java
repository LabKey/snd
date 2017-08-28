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

import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.DbSequence;
import org.labkey.api.data.DbSequenceManager;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
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
import org.labkey.api.query.UserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
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

    private SNDManager()
    {
        // prevent external construction with a private default constructor
    }

    public static SNDManager get()
    {
        return _instance;
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

    public boolean isInUse(Container c, User u, int pkgId)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = schema.getTable(SNDSchema.PKGS_TABLE_NAME);
        TableSelector ts = new TableSelector(pkgsTable, Collections.singleton("HasData"), new SimpleFilter(FieldKey.fromString("PkgId"), pkgId), null);
        Boolean[] ret = ts.getArray(Boolean.class);
        return ret[0];
    }

    public void deletePackageCategories(Container c, User u, int pkgId)
    {
        SQLFragment sql = new SQLFragment("DELETE FROM snd.PkgCategoryJunction WHERE PkgId = " + pkgId);
        SqlExecutor sqlex = new SqlExecutor(SNDSchema.getInstance().getSchema());
        sqlex.execute(sql);
    }

    public void updatePackage(User u, Container c, Package pkg, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = schema.getTable(SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = pkgsTable.getUpdateService();
        if (pkgQus == null)
            throw new IllegalStateException();

        TableInfo pkgCategJuncTable = schema.getTable(SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME);
        QueryUpdateService pkgCategoryQus = pkgCategJuncTable.getUpdateService();
        if (pkgCategoryQus == null)
            throw new IllegalStateException();

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

        if (!errors.hasErrors())
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

        TableInfo pkgsTable = schema.getTable(SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = pkgsTable.getUpdateService();
        if (pkgQus == null)
            throw new IllegalStateException();

        TableInfo pkgCategJuncTable = schema.getTable(SNDSchema.PKGCATEGORYJUNCTION_TABLE_NAME);
        QueryUpdateService pkgCategoryQus = pkgCategJuncTable.getUpdateService();
        if (pkgCategoryQus == null)
            throw new IllegalStateException();

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
            GWTDomain<GWTPropertyDescriptor> newDomain = new GWTDomain<>();
            newDomain.setName(getPackageName(pkg.getPkgId()));
            newDomain.setContainer(c.getId());
            newDomain.setDescription(pkg.getDescription());
            newDomain.setFields(pkg.getAttributes());

            DomainUtil.createDomain(PackageDomainKind.getPackageKindName(), newDomain, null, c, u, null, null);
        }
    }

    public void createSuperPackage(User u, Container c, SuperPackage superPkg, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo superPkgsTable = schema.getTable(SNDSchema.SUPERPKGS_TABLE_NAME);
        QueryUpdateService superPkgQus = superPkgsTable.getUpdateService();
        if (superPkgQus == null)
            throw new IllegalStateException();

        List<Map<String, Object>> superPkgRows = new ArrayList<>();
        superPkgRows.add(superPkg.getSuperPackageRow(c));

        try (DbScope.Transaction tx = superPkgsTable.getSchema().getScope().ensureTransaction())
        {
            superPkgQus.insertRows(u, c, superPkgRows, errors, null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | DuplicateKeyException | SQLException e)
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
        sql.append(" WHERE Container = ? AND PkgId = ?").add(c).add(pkgId);
        SqlSelector selector = new SqlSelector(schema.getDbSchema(), sql);
        return selector.getArrayList(Integer.class);
    }

    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, BatchValidationException errors)
    {
        UserSchema schema = QueryService.get().getUserSchema(u, c, SNDSchema.NAME);

        TableInfo pkgsTable = schema.getTable(SNDSchema.PKGS_TABLE_NAME);
        QueryUpdateService pkgQus = pkgsTable.getUpdateService();
        if (pkgQus == null)
            throw new IllegalStateException();

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
            Package pkg;
            for (Map<String, Object> row : rows)
            {
                pkg = new Package();
                pkg.setPkgId((Integer) row.get(Package.PKG_ID));
                pkg.setDescription((String) row.get(Package.PKG_DESCRIPTION));
                pkg.setActive((boolean) row.get(Package.PKG_ACTIVE));
                pkg.setRepeatable((boolean) row.get(Package.PKG_REPEATABLE));
                pkg.setNarrative((String) row.get(Package.PKG_NARRATIVE));
                pkg.setQcState((Integer) row.get(Package.PKG_QCSTATE));
                pkg.setCategories(getPackageCategories(c, u, pkg.getPkgId()));
                pkg.setAttributes(getPackageAttributes(c, u, pkg.getPkgId()));

                packages.add(pkg);
            }
        }

        return packages;
    }
}