package org.labkey.snd.security;

import org.labkey.api.security.permissions.AbstractPermission;

public class SNDReviewRequestedInsertPermission extends AbstractPermission
{
    public SNDReviewRequestedInsertPermission()
    {
        super("Insert Review Requested SND Data", "Allows inserting SND data with QC state Review Requested");
    }
}
