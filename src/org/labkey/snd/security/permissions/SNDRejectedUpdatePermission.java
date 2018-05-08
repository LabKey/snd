package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDRejectedUpdatePermission extends SNDQCStatePermission
{
    public SNDRejectedUpdatePermission()
    {
        super("Update Rejected SND Data", "Allows updating SND data with QC state Rejected", QCStateEnum.REJECTED);
    }
}