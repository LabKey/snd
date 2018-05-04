package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDInProgressReadPermission extends SNDQCStatePermission
{
    public SNDInProgressReadPermission()
    {
        super("Read In Progress SND Data", "Allows reading SND data with QC state In Progress", QCStateEnum.IN_PROGRESS);
    }
}

