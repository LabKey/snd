package org.labkey.snd.security;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;

public class SNDDataAdminRole extends AbstractModuleScopedRole
{
    public SNDDataAdminRole()
    {
        super("SND Data Admin", "SND Data Admin may read, insert, update and delete SND data with all QC states.",
                SNDModule.class,
                SNDInProgressReadPermission.class, SNDInProgressUpdatePermission.class,
                SNDInProgressInsertPermission.class, SNDInProgressDeletePermission.class,
                SNDReviewRequestedUpdatePermission.class, SNDReviewRequestedReadPermission.class,
                SNDReviewRequestedInsertPermission.class, SNDReviewRequestedDeletePermission.class,
                SNDCompletedReadPermission.class, SNDCompletedUpdatePermission.class,
                SNDCompletedInsertPermission.class, SNDCompletedDeletePermission.class);
    }

    @Override
    public boolean isApplicable(SecurityPolicy policy, SecurableResource resource)
    {
        return true;
//        return super.isApplicable(policy,resource) || resource instanceof Dataset;
    }
}
