package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDRejectedDeletePermission extends SNDQCStatePermission
{
    public SNDRejectedDeletePermission()
    {
        super("Delete Rejected SND Data", "Allows deleting SND data with QC state Rejected", QCStateEnum.REJECTED);
    }
}
