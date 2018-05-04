package org.labkey.snd.security.permissions;

import org.labkey.api.snd.QCStateEnum;

public class SNDReviewRequiredUpdatePermission extends SNDQCStatePermission
{
    public SNDReviewRequiredUpdatePermission()
    {
        super("Update Review Requested SND Data", "Allows updating SND data with QC state Review Requested", QCStateEnum.REVIEW_REQUIRED);
    }
}
