package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDRejectedDeletePermission extends AbstractPermission
{
    public SNDRejectedDeletePermission()
    {
        super("Delete Rejected SND Data", "Allows deleting SND data with QC state Rejected");
    }
}
