package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDCompletedInsertPermission extends SNDQCStatePermission
{
    public SNDCompletedInsertPermission()
    {
        super("Insert Completed SND Data", "Allows inserting SND data with QC state Completed", QCStateEnum.COMPLETED);
    }
}
