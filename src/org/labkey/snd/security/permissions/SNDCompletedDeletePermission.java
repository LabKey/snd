package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDCompletedDeletePermission extends SNDQCStatePermission
{
    public SNDCompletedDeletePermission()
    {
        super("Delete Completed SND Data", "Allows deleting SND data with QC state Completed", QCStateEnum.COMPLETED);
    }
}
