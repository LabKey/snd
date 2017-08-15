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
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainKind;
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

import java.sql.SQLException;
import java.util.ArrayList;
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

    private String getPackageName(int id)
    {
        return "Package-" + id;
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
            pkgCategoryQus.updateRows(u, c, pkg.getCategoryRows(c), null, null, null);
            tx.commit();
        }
        catch (QueryUpdateServiceException | BatchValidationException | InvalidKeyException | SQLException e)
        {
            errors.addRowError(new ValidationException(e.getMessage()));
        }

//        String domainURI =
//        GWTDomain existingDomain = DomainUtil.getDomainDescriptor(u, domainURI, c);
    }

    public void createNewPackage(User u, Container c, Package pkg, BatchValidationException errors)
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

        GWTDomain<GWTPropertyDescriptor> newDomain = new GWTDomain<>();
        newDomain.setName(getPackageName(pkg.getPkgId()));
        newDomain.setContainer(c.getId());
        newDomain.setDescription(pkg.getDescription());
        newDomain.setFields(pkg.getAttributes());

        DomainKind kind = new PackageDomainKind();
        Domain domain = DomainUtil.createDomain(kind.getKindName(), newDomain, null, c, u, null, null);

    }
}