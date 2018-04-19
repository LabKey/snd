package org.labkey.snd.security.roles;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;
import org.labkey.snd.security.permissions.SNDCompletedDeletePermission;
import org.labkey.snd.security.permissions.SNDCompletedInsertPermission;
import org.labkey.snd.security.permissions.SNDCompletedReadPermission;
import org.labkey.snd.security.permissions.SNDCompletedUpdatePermission;
import org.labkey.snd.security.permissions.SNDInProgressDeletePermission;
import org.labkey.snd.security.permissions.SNDInProgressInsertPermission;
import org.labkey.snd.security.permissions.SNDInProgressReadPermission;
import org.labkey.snd.security.permissions.SNDInProgressUpdatePermission;
import org.labkey.snd.security.permissions.SNDReviewRequestedDeletePermission;
import org.labkey.snd.security.permissions.SNDReviewRequestedInsertPermission;
import org.labkey.snd.security.permissions.SNDReviewRequestedReadPermission;
import org.labkey.snd.security.permissions.SNDReviewRequestedUpdatePermission;

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
