package org.labkey.snd.security;

import org.labkey.api.security.roles.Role;
import org.labkey.snd.security.roles.SNDBasicSubmitterRole;
import org.labkey.snd.security.roles.SNDDataAdminRole;
import org.labkey.snd.security.roles.SNDDataReviewerRole;
import org.labkey.snd.security.roles.SNDReaderRole;

public enum SecurityRolesEnum
{
    BasicSubmitterRole(new SNDBasicSubmitterRole()),
    DataReviewerRole(new SNDDataReviewerRole()),
    DataAdminRole(new SNDDataAdminRole()),
    ReaderRole(new SNDReaderRole());

    Role _role;

    SecurityRolesEnum(Role role)
    {
        _role = role;
    }

    Role getRole()
    {
        return _role;
    }
}
