package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDInProgressUpdatePermission extends AbstractPermission
{
    public SNDInProgressUpdatePermission()
    {
        super("Update In Progress SND Data", "Allows updating SND data with QC state In Progress");
    }
}
