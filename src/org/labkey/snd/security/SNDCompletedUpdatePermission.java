package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDCompletedUpdatePermission extends AbstractPermission
{
    public SNDCompletedUpdatePermission()
    {
        super("Update Completed SND Data", "Allows updating SND data with QC state Completed");
    }
}
