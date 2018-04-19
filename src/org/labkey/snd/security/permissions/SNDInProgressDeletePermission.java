package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDInProgressDeletePermission extends AbstractPermission
{
    public SNDInProgressDeletePermission()
    {
        super("Delete In Progress SND Data", "Allows deleting SND data with QC state In Progress");
    }
}
