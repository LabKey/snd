package org.labkey.api.snd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.security.HasPermission;
import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.SecurityPolicyManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.util.GUID;

import java.util.List;

public class Category implements SecurableResource, HasPermission
{
    private int _categoryId;
    private String _description;
    private boolean _active;
    private GUID _objectId;
    private Container _container;

    public int getCategoryId()
    {
        return _categoryId;
    }

    public void setCategoryId(int categoryId)
    {
        _categoryId = categoryId;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public GUID getObjectId()
    {
        return _objectId;
    }

    public void setObjectId(GUID objectId)
    {
        _objectId = objectId;
    }

    public Container getContainer()
    {
        return _container;
    }

    public void setContainer(Container container)
    {
        _container = container;
    }

    @Override
    public @NotNull String getResourceId()
    {
        return _objectId.toString();
    }

    @Override
    public @NotNull String getResourceName()
    {
        return "SND.Category." + _categoryId;
    }

    @Override
    public @NotNull String getResourceDescription()
    {
        return _description;
    }

    @Override
    public @NotNull Module getSourceModule()
    {
        return ModuleLoader.getInstance().getModule("SND");
    }

    @Override
    public @Nullable SecurableResource getParentResource()
    {
        return null;
    }

    @Override
    public @NotNull Container getResourceContainer()
    {
        return _container;
    }

    @Override
    public @NotNull List<SecurableResource> getChildResources(User user)
    {
        return null;
    }

    @Override
    public boolean mayInheritPolicy()
    {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return SecurityPolicyManager.getPolicy(this).hasPermission("User does not have permission", user, perm);
    }
}
