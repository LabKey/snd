package org.labkey.snd.security.permissions;

import org.labkey.api.security.permissions.AbstractPermission;
import org.labkey.snd.SNDModule;
import org.labkey.api.snd.QCStateEnum;

public class SNDQCStatePermission extends AbstractPermission
{
    private QCStateEnum _qcState;

    SNDQCStatePermission(String name, String description, QCStateEnum qcState)
    {
        super(name, description, SNDModule.class);
        _qcState = qcState;
    }

    public QCStateEnum getQCState()
    {
        return _qcState;
    }
}
