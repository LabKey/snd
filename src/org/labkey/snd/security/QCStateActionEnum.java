package org.labkey.snd.security;

import org.jetbrains.annotations.Nullable;
import org.labkey.snd.security.permissions.SNDCompletedDeletePermission;
import org.labkey.snd.security.permissions.SNDCompletedInsertPermission;
import org.labkey.snd.security.permissions.SNDCompletedReadPermission;
import org.labkey.snd.security.permissions.SNDCompletedUpdatePermission;
import org.labkey.snd.security.permissions.SNDInProgressDeletePermission;
import org.labkey.snd.security.permissions.SNDInProgressInsertPermission;
import org.labkey.snd.security.permissions.SNDInProgressReadPermission;
import org.labkey.snd.security.permissions.SNDInProgressUpdatePermission;
import org.labkey.snd.security.permissions.SNDQCStatePermission;
import org.labkey.snd.security.permissions.SNDRejectedDeletePermission;
import org.labkey.snd.security.permissions.SNDRejectedInsertPermission;
import org.labkey.snd.security.permissions.SNDRejectedReadPermission;
import org.labkey.snd.security.permissions.SNDRejectedUpdatePermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredDeletePermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredInsertPermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredReadPermission;
import org.labkey.snd.security.permissions.SNDReviewRequiredUpdatePermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum QCStateActionEnum
{
    INSERT(new SNDCompletedInsertPermission(), new SNDInProgressInsertPermission(), new SNDRejectedInsertPermission(), new SNDReviewRequiredInsertPermission()),
    UPDATE(new SNDCompletedUpdatePermission(), new SNDInProgressUpdatePermission(), new SNDRejectedUpdatePermission(), new SNDReviewRequiredUpdatePermission()),
    DELETE(new SNDCompletedDeletePermission(), new SNDInProgressDeletePermission(), new SNDRejectedDeletePermission(), new SNDReviewRequiredDeletePermission()),
    READ(new SNDCompletedReadPermission(), new SNDInProgressReadPermission(), new SNDRejectedReadPermission(), new SNDReviewRequiredReadPermission());

    private List<SNDQCStatePermission> _permissions = new ArrayList<>();

    QCStateActionEnum(SNDQCStatePermission... perms)
    {
        _permissions.addAll(Arrays.asList(perms));
    }

    @Nullable
    public SNDQCStatePermission getPermission(QCStateEnum qcState)
    {
        for (SNDQCStatePermission permission : _permissions)
        {
            if (permission.getQCState() == qcState)
            {
                return permission;
            }
        }

        return null;
    }
}
