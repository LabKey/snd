package org.labkey.snd.security.roles;

import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicy;
import org.labkey.api.security.roles.AbstractModuleScopedRole;
import org.labkey.snd.SNDModule;
import org.labkey.snd.security.permissions.SNDCompletedReadPermission;

public class SNDReaderRole extends AbstractModuleScopedRole
{
    public SNDReaderRole()
    {
        super("SND Reader", "SND Reader may read SND data with QC state Completed.",
                SNDModule.class,
                SNDCompletedReadPermission.class);
    }

    @Override
    public boolean isApplicable(SecurityPolicy policy, SecurableResource resource)
    {
        return true;
//        return super.isApplicable(policy,resource) || resource instanceof Dataset;
    }
}
