package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDCompletedInsertPermission extends AbstractPermission
{
    public SNDCompletedInsertPermission()
    {
        super("Insert Completed SND Data", "Allows inserting SND data with QC state Completed");
    }
}
