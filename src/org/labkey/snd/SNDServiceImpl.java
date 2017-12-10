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

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
import org.labkey.api.snd.Project;
import org.labkey.api.snd.SNDSequencer;
import org.labkey.api.snd.SNDService;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.util.UnexpectedException;

import java.util.List;
import java.util.Map;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDServiceImpl implements SNDService
{
    public static final SNDServiceImpl INSTANCE = new SNDServiceImpl();

    private SNDServiceImpl()
    {
    }

    @Override
    public void savePackage(Container c, User u, Package pkg)
    {
        savePackage(c, u, pkg, null, false);
    }

    @Override
    public void savePackage(Container c, User u, Package pkg, SuperPackage superPkg, boolean cloneFlag)
    {
        BatchValidationException errors = new BatchValidationException();
        Domain domain = null;

        if (null != pkg.getPkgId() && pkg.getPkgId() > -1)
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), SNDManager.getPackageName(pkg.getPkgId()), c, u);
            domain = PropertyService.get().getDomain(c, domainURI);
        }
        if ((null != domain) && !cloneFlag)  // clone case is basically creation
        {
            SNDManager.get().updatePackage(u, c, pkg, superPkg, errors);
        }
        else
        {
            pkg.setPkgId(SNDSequencer.PKGID.ensureId(c, pkg.getPkgId()));

            if (superPkg != null)
                superPkg.setPkgId(pkg.getPkgId());

            SNDManager.get().createPackage(u, c, pkg, superPkg, errors);
        }
        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }

    @Override
    public void saveSuperPackages(Container c, User u, List<SuperPackage> superPkgs)
    {
        BatchValidationException errors = new BatchValidationException();

        SNDManager.get().saveSuperPackages(u, c, superPkgs, errors);

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }

    @Override
    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds, boolean includeExtraFields, boolean includeLookups)
    {
        BatchValidationException errors = new BatchValidationException();

        List<Package> pkgs = SNDManager.get().getPackages(c, u, pkgIds, includeExtraFields, includeLookups, errors);
        if (errors.hasErrors())
            throw new UnexpectedException(errors);

        return pkgs;
    }

    @Override
    public void registerAttributeLookup(Container c, User u, String schema, @Nullable String table)
    {
        SNDManager.get().registerAttributeLookups(c, u, schema, table);
    }

    @Override
    public Map<String, String> getAttributeLookups(Container c, User u)
    {
        return SNDManager.get().getAttributeLookups(c, u);
    }

    @Override
    public Object getDefaultLookupDisplayValue(User u, Container c, String schema, String table, Object key)
    {
        return SNDManager.get().getDefaultLookupDisplayValue(u, c, schema, table, key);
    }

    public void saveProject(Container c, User u, Project project)
    {
        BatchValidationException errors = new BatchValidationException();

        SNDManager.get().createProject(c, u, project, errors);

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }
}
