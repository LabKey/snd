package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.SNDPackage;
import org.labkey.api.snd.SNDService;
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
    public void savePackage(Container c, User u, SNDPackage pkg)
    {
        BatchValidationException errors = new BatchValidationException();

        // Create/update package in pkgs table
        if (null != pkg.getPkgId() && pkg.getPkgId() > 0)
        {
            SNDManager.get().updatePackage(u, c, pkg, errors);
        }
        else
        {
            pkg.setPkgId(SNDManager.get().generatePackageId(c));
            SNDManager.get().createNewPackage(u, c, pkg, errors);
        }

        if (errors.hasErrors())
            throw new UnexpectedException(errors);
    }
}
