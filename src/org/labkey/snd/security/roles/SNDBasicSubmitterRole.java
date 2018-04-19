package org.labkey.snd.security.roles;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;
import org.labkey.snd.security.permissions.SNDInProgressDeletePermission;
import org.labkey.snd.security.permissions.SNDInProgressInsertPermission;
import org.labkey.snd.security.permissions.SNDInProgressReadPermission;
import org.labkey.snd.security.permissions.SNDInProgressUpdatePermission;

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