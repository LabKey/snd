package org.labkey.snd.security.roles;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.RoleSet;
import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.User;
import org.labkey.api.security.impersonation.ImpersonationContextFactory;
import org.labkey.api.security.impersonation.RoleImpersonationContextFactory;
import org.labkey.api.security.roles.Role;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Stream;

public class SNDRoleImpersonationContext extends RoleImpersonationContextFactory.RoleImpersonationContext
{
    public SNDRoleImpersonationContext(@Nullable Container project, User adminUser, RoleSet roles, ImpersonationContextFactory factory, String cacheKey)
    {
        super(project, adminUser, roles, factory, cacheKey);
    }

    @Override
    public Stream<Role> getAssignedRoles(User user, SecurableResource resource)
    {
        RoleSet _roles = Objects.requireNonNullElse(getRoles(), new RoleSet(new HashSet<>()));
        return _roles.stream();
    }
}
