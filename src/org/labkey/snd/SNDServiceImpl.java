package org.labkey.snd;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Package;
import org.labkey.api.snd.PackageDomainKind;
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
        BatchValidationException errors = new BatchValidationException();
        Domain domain = null;

        if (null != pkg.getPkgId() && pkg.getPkgId() > -1)
        {
            String domainURI = PackageDomainKind.getDomainURI(PackageDomainKind.getPackageSchemaName(), SNDManager.getPackageName(pkg.getPkgId()), c, u);
            domain = PropertyService.get().getDomain(c, domainURI);
        }

        // Create/update package in pkgs table
        if (null != domain)
        {
            SNDManager.get().updatePackage(u, c, pkg, errors);
        }
        else
        {
            if (null == pkg.getPkgId() || pkg.getPkgId() < 0)
                pkg.setPkgId(SNDManager.get().generatePackageId(c));

            SNDManager.get().createPackage(u, c, pkg, errors);
        }

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }

    @Override
    public void saveSuperPackages(Container c, User u, List<SuperPackage> superPkgs)
    {
        BatchValidationException errors = new BatchValidationException();

        SNDManager.get().createSuperPackages(u, c, superPkgs, errors);

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }

    @Override
    public List<Package> getPackages(Container c, User u, List<Integer> pkgIds)
    {
        BatchValidationException errors = new BatchValidationException();

        List<Package> pkgs = SNDManager.get().getPackages(c, u, pkgIds, errors);
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
}
