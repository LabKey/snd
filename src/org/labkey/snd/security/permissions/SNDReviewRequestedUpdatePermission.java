package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDReviewRequestedUpdatePermission extends AbstractPermission
{
    public SNDReviewRequestedUpdatePermission()
    {
        super("Update Review Requested SND Data", "Allows updating SND data with QC state Review Requested");
    }
}
