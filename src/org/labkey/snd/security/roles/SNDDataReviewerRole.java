package org.labkey.snd.security.roles;

import org.labkey.api.module.ModuleLoader;
import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.api.snd.Category;
import org.labkey.snd.SNDModule;
import org.labkey.snd.security.permissions.SNDCompletedReadPermission;
import org.labkey.snd.security.permissions.SNDCompletedUpdatePermission;
import org.labkey.snd.security.permissions.SNDInProgressReadPermission;
import org.labkey.snd.security.permissions.SNDRejectedReadPermission;
import org.labkey.snd.security.permissions.SNDRejectedUpdatePermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredReadPermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredUpdatePermission;

public class SNDDataReviewerRole extends AbstractModuleScopedRole
{
    public SNDDataReviewerRole()
    {
        super("SND Data Reviewer", "SND Data Reviewer may read and update SND data with QC states In Progress, Review Requested and Completed.",
                SNDModule.class,
                SNDInProgressReadPermission.class,
                SNDReviewRequiredReadPermission.class, SNDReviewRequiredUpdatePermission.class,
                SNDCompletedReadPermission.class, SNDCompletedUpdatePermission.class,
                SNDRejectedReadPermission.class, SNDRejectedUpdatePermission.class);
    }

    @Override
    public boolean isApplicable(SecurityPolicy policy, SecurableResource resource)
    {
        return resource instanceof Category && ((Category)resource).getContainer().getActiveModules().contains(ModuleLoader.getInstance().getModule(SNDModule.class));
    }
}