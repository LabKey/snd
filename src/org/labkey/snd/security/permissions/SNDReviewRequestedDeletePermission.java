package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDReviewRequestedDeletePermission extends AbstractPermission
{
    public SNDReviewRequestedDeletePermission()
    {
        super("Delete Review Requested SND Data", "Allows deleting SND data with QC state Review Requested");
    }
}

