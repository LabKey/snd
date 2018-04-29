package org.labkey.snd.security.permissions;

import org.labkey.snd.security.QCStateEnum;

public class SNDReviewRequiredInsertPermission extends SNDQCStatePermission
{
    public SNDReviewRequiredInsertPermission()
    {
        super("Insert Review Requested SND Data", "Allows inserting SND data with QC state Review Requested", QCStateEnum.REVIEW_REQUIRED);
    }
}
