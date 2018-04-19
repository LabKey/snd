package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDInProgressReadPermission extends AbstractPermission
{
    public SNDInProgressReadPermission()
    {
        super("Read In Progress SND Data", "Allows reading SND data with QC state In Progress");
    }
}

