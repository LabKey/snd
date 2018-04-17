package org.labkey.snd.security;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;

public class SNDDataReviewerRole extends AbstractModuleScopedRole
{
    public SNDDataReviewerRole()
    {
        super("SND Data Reviewer", "SND Data Reviewer may read and update SND data with QC states In Progress, Review Requested and Completed.",
                SNDModule.class,
                SNDInProgressReadPermission.class, SNDInProgressUpdatePermission.class,
                SNDReviewRequestedUpdatePermission.class, SNDReviewRequestedReadPermission.class,
                SNDCompletedReadPermission.class, SNDCompletedUpdatePermission.class);
    }

    @Override
    public boolean isApplicable(SecurityPolicy policy, SecurableResource resource)
    {
        return true;
//        return super.isApplicable(policy,resource) || resource instanceof Dataset;
    }
}