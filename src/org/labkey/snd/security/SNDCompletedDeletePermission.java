package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDCompletedDeletePermission extends AbstractPermission
{
    public SNDCompletedDeletePermission()
    {
        super("Delete Completed SND Data", "Allows deleting SND data with QC state Completed");
    }
}
