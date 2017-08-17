package org.labkey.snd;

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

        if (null != pkg.getPkgId() && pkg.getPkgId() > 0)
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
            if(null == pkg.getPkgId())
                pkg.setPkgId(SNDManager.get().generatePackageId(c));

            SNDManager.get().createNewPackage(u, c, pkg, errors);
        }

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }

    @Override
    public void saveSuperPackage(Container c, User u, SuperPackage superPkg)
    {

    }
}
