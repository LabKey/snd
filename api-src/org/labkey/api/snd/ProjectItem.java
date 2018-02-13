package org.labkey.api.snd;

import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.util.Map;

public class ProjectItem
{
    boolean _active;
    int _projectItemId;
    int _superPkgId;
    String _parentObjectId;
    String _container;
    SuperPackage _superPackage;

    public static final String PROJECTITEM_ID = "projectItemId";
    public static final String PROJECTITEM_PARENTOBJECTID = "parentObjectId";
    public static final String PROJECTITEM_ACTIVE = "active";
    public static final String PROJECTITEM_CONTAINER = "container";
    public static final String PROJECTITEM_SUPERPKGID = "superPkgId";
    public static final String PROJECTITEM_SUPERPKG = "superPkg";

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

    public String getContainer()
    {
        return _container;
    }

    public void setContainer(String container)
    {
        _container = container;
    }

    public SuperPackage getSuperPackage()
    {
        return _superPackage;
    }

    public void setSuperPackage(SuperPackage superPackage)
    {
        _superPackage = superPackage;
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

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(PROJECTITEM_ID, getProjectItemId());
        json.put(PROJECTITEM_ACTIVE, isActive());
        json.put(PROJECTITEM_SUPERPKG, getSuperPackage().toJSON(c, u));

        return json;
    }
}
