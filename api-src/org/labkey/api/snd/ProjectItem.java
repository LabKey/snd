package org.labkey.api.snd;

import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;

import java.util.Map;

public class ProjectItem
{
    boolean _active;
    int _projectItemId;
    int _superPkgId;
    String _parentObjectId;

    public static final String PROJECTITEM_ID = "ProjectItemId";
    public static final String PROJECTITEM_PARENTOBJECTID = "ParentObjectId";
    public static final String PROJECTITEM_ACTIVE = "Active";
    public static final String PROJECTITEM_CONTAINER = "Container";
    public static final String PROJECTITEM_SUPERPKGID = "SuperPkgId";

    public int getProjectItemId()
    {
        return _projectItemId;
    }

    public void setProjectItemId(int projectItemId)
    {
        _projectItemId = projectItemId;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        _active = active;
    }

    public int getSuperPkgId()
    {
        return _superPkgId;
    }

    public void setSuperPkgId(int superPkgId)
    {
        _superPkgId = superPkgId;
    }

    public String getParentObjectId()
    {
        return _parentObjectId;
    }

    public void setParentObjectId(String parentObjectId)
    {
        _parentObjectId = parentObjectId;
    }

    public Map<String, Object> getRow(Container c)
    {
        Map<String, Object> projectItemValues = new ArrayListMap<>();
        projectItemValues.put(PROJECTITEM_ID, getProjectItemId());
        projectItemValues.put(PROJECTITEM_PARENTOBJECTID, getParentObjectId());
        projectItemValues.put(PROJECTITEM_SUPERPKGID, getSuperPkgId());
        projectItemValues.put(PROJECTITEM_ACTIVE, isActive());
        projectItemValues.put(PROJECTITEM_CONTAINER, c);

        return projectItemValues;
    }
}
