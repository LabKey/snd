package org.labkey.api.snd;

import org.labkey.api.data.Container;
import org.labkey.api.query.ExtendedTableDomainKind;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;

/**
 * Created by marty on 8/14/2017.
 */
public class PackageDomainKind extends ExtendedTableDomainKind
{
    private final String NAMESPACE_PREFIX = "package";
    private final String SCHEMA_NAME = "snd";
    private final String KIND_NAME = "Package";

    @Override
    public boolean canCreateDefinition(User user, Container container)
    {
        return container.hasPermission("PackageDomainKind.canCreateDefinition", user, AdminPermission.class);
    }

    @Override
    protected String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    @Override
    protected String getNamespacePrefix()
    {
        return NAMESPACE_PREFIX;
    }

    @Override
    public String getKindName()
    {
        return KIND_NAME;
    }
}
