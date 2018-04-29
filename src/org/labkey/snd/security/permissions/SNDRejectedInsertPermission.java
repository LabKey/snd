package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDRejectedInsertPermission extends SNDQCStatePermission
{
    public SNDRejectedInsertPermission()
    {
        super("Insert Rejected SND Data", "Allows inserting SND data with QC state Rejected", QCStateEnum.REJECTED);
    }
}