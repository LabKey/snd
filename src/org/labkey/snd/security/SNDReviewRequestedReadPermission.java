package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDReviewRequestedReadPermission extends AbstractPermission
{
    public SNDReviewRequestedReadPermission()
    {
        super("Read Review Requested SND Data", "Allows reading SND data with QC state Review Requested");
    }
}
