package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDInProgressInsertPermission extends AbstractPermission
{
    public SNDInProgressInsertPermission()
    {
        super("Insert In Progress SND Data", "Allows inserting SND data with QC state In Progress");
    }
}
