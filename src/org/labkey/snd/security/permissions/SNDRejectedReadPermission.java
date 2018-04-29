package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDRejectedReadPermission extends SNDQCStatePermission
{
    public SNDRejectedReadPermission()
    {
        super("Read Rejected SND Data", "Allows reading SND data with QC state Rejected", QCStateEnum.REJECTED);
    }
}
