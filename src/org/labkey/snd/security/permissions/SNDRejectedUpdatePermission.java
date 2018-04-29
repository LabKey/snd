package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDRejectedUpdatePermission extends AbstractPermission
{
    public SNDRejectedUpdatePermission()
    {
        super("Update Rejected SND Data", "Allows updating SND data with QC state Rejected");
    }
}
