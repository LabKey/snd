package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDRejectedInsertPermission extends AbstractPermission
{
    public SNDRejectedInsertPermission()
    {
        super("Insert Rejected SND Data", "Allows inserting SND data with QC state Rejected");
    }
}