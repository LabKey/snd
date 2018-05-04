package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDCompletedUpdatePermission extends SNDQCStatePermission
{
    public SNDCompletedUpdatePermission()
    {
        super("Update Completed SND Data", "Allows updating SND data with QC state Completed", QCStateEnum.COMPLETED);
    }
}
