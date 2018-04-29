package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDReviewRequiredDeletePermission extends SNDQCStatePermission
{
    public SNDReviewRequiredDeletePermission()
    {
        super("Delete Review Requested SND Data", "Allows deleting SND data with QC state Review Requested", QCStateEnum.REVIEW_REQUIRED);
    }
}

