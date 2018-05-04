package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDInProgressInsertPermission extends SNDQCStatePermission
{
    public SNDInProgressInsertPermission()
    {
        super("Insert In Progress SND Data", "Allows inserting SND data with QC state In Progress", QCStateEnum.IN_PROGRESS);
    }
}
