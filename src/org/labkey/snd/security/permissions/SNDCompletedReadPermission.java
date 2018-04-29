package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDCompletedReadPermission extends SNDQCStatePermission
{
    public SNDCompletedReadPermission()
    {
        super("Read Completed SND Data", "Allows reading SND data with QC state Completed", QCStateEnum.COMPLETED);
    }
}
