package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDInProgressUpdatePermission extends SNDQCStatePermission
{
    public SNDInProgressUpdatePermission()
    {
        super("Update In Progress SND Data", "Allows updating SND data with QC state In Progress", QCStateEnum.IN_PROGRESS);
    }
}
