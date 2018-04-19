package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDCompletedReadPermission extends AbstractPermission
{
    public SNDCompletedReadPermission()
    {
        super("Read Completed SND Data", "Allows reading SND data with QC state Completed");
    }
}
