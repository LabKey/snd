package org.labkey.snd.security;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;

public class SNDBasicSubmitterRole extends AbstractModuleScopedRole
{
    public SNDBasicSubmitterRole()
    {
        super("SND Basic Submitter", "SND Basic Submitters may read, add, update and delete SND data with QC state In Progress.",
                SNDModule.class,
                SNDInProgressReadPermission.class, SNDInProgressInsertPermission.class,
                SNDInProgressUpdatePermission.class, SNDInProgressDeletePermission.class);
    }

    @Override
    public boolean isApplicable(SecurityPolicy policy, SecurableResource resource)
    {
        return true;
//        return super.isApplicable(policy,resource) || resource instanceof Dataset;
    }
}