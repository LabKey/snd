package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDReviewRequiredReadPermission extends SNDQCStatePermission
{
    public SNDReviewRequiredReadPermission()
    {
        super("Read Review Requested SND Data", "Allows reading SND data with QC state Review Requested", QCStateEnum.REVIEW_REQUIRED);
    }
}
