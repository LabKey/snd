package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDInProgressDeletePermission extends SNDQCStatePermission
{
    public SNDInProgressDeletePermission()
    {
        super("Delete In Progress SND Data", "Allows deleting SND data with QC state In Progress", QCStateEnum.IN_PROGRESS);
    }
}
