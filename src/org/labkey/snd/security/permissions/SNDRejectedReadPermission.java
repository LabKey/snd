package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDRejectedReadPermission extends AbstractPermission
{
    public SNDRejectedReadPermission()
    {
        super("Read Rejected SND Data", "Allows reading SND data with QC state Rejected");
    }
}
