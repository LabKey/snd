package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;
import org.labkey.api.util.GUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project
{
    private int _projectId;
    private int _revisionNum;
    private String _objectId;
    private Date _startDate;
    private Date  _endDate;
    private String _description;
    private boolean _active;
    private int _referenceId;
    private List<ProjectItem> _projectItems;
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();

    private String _container;
    private String _revisedObjectId;
    private Integer _revisedRevNum;

    public static final String PROJECT_ID = "projectId";
    public static final String PROJECT_DESCRIPTION = "description";
    public static final String PROJECT_ACTIVE = "active";
    public static final String PROJECT_STARTDATE = "startDate";
    public static final String PROJECT_ENDDATE = "endDate";
    public static final String PROJECT_OBJECTID = "objectId";
    public static final String PROJECT_CONTAINER = "container";
    public static final String PROJECT_REVNUM = "revisionNum";
    public static final String PROJECT_REFID = "referenceId";
    public static final String PROJECT_ITEMS = "projectItems";

    public static final String PROJECT_HASEVENT = "hasEvent";

    public Project (int id, Integer revNum, boolean edit, boolean revision, Container c)
    {
        _projectId = ((edit || revision) ? id : SNDSequencer.PROJECTID.ensureId(c, id));
        _objectId = ((edit || revision) ? null : GUID.makeGUID());
        _revisionNum = ((edit || revision) ? revNum : 0);

        if (revision)
        {
            _revisedObjectId = GUID.makeGUID();
            _revisedRevNum = ++revNum;
        }
    }

    public Project () {}

    public int getProjectId()
    {
        return _projectId;
    }

    public void setProjectId(int projectId)
    {
        _projectId = projectId;
    }

    public int getReferenceId()
    {
        return _referenceId;
    }

    public void setReferenceId(int refId)
    {
        _referenceId = refId;
    }

    public String getContainer()
    {
        return _container;
    }

    public void setContainer(String container)
    {
        _container = container;
    }

    public int getRevisionNum()
    {
        return _revisionNum;
    }

    public void setRevisionNum(int revisionNum)
    {
        _revisionNum = revisionNum;
    }

    public String getObjectId()
    {
        return _objectId;
    }

    public void setObjectId(String objectId)
    {
        _objectId = objectId;
    }

    public String getRevisedObjectId()
    {
        return _revisedObjectId;
    }

    public void setRevisedObjectId(String revisedObjectId)
    {
        _revisedObjectId = revisedObjectId;
    }

    public Integer getRevisedRevNum()
    {
        return _revisedRevNum;
    }

    public void setRevisedRevNum(Integer revisedRevNum)
    {
        _revisedRevNum = revisedRevNum;
    }

    public Date getStartDate()
    {
        return _startDate;
    }

    public void setStartDate(Date startDate)
    {
        _startDate = startDate;
    }

    public Date getEndDate()
    {
        return _endDate;
    }

    public void setEndDate(Date endDate)
    {
        _endDate = endDate;
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

    public List<ProjectItem> getProjectItems()
    {
        return _projectItems;
    }

    public void setProjectItems(List<ProjectItem> subpackages)
    {
        _projectItems = subpackages;
    }

    public Map<GWTPropertyDescriptor, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Map<GWTPropertyDescriptor, Object> extraFields)
    {
        _extraFields = extraFields;
    }

    public Map<String, Object> getProjectRow(Container c)
    {
        Map<String, Object> projectValues = new ArrayListMap<>();
        projectValues.put(PROJECT_ID, getProjectId());
        projectValues.put(PROJECT_OBJECTID, getObjectId());
        projectValues.put(PROJECT_REVNUM, getRevisionNum());
        projectValues.put(PROJECT_DESCRIPTION, getDescription());
        projectValues.put(PROJECT_ACTIVE, isActive());
        projectValues.put(PROJECT_STARTDATE, getStartDate());
        projectValues.put(PROJECT_ENDDATE, getEndDate());
        projectValues.put(PROJECT_REFID, getReferenceId());
        projectValues.put(PROJECT_CONTAINER, c);

        Map<GWTPropertyDescriptor, Object> extras = getExtraFields();
        for (GWTPropertyDescriptor gpd : extras.keySet())
        {
            projectValues.put(gpd.getName(), extras.get(gpd));
        }

        return projectValues;
    }

    public List<Map<String, Object>> getProjectItemRows(Container c)
    {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (ProjectItem projectItem : getProjectItems())
        {
            rows.add(projectItem.getRow(c));
        }

        return rows;
    }

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(PROJECT_ID, getProjectId());
        json.put(PROJECT_DESCRIPTION, getDescription());
        json.put(PROJECT_ACTIVE, isActive());
        json.put(PROJECT_STARTDATE, getStartDate());
        json.put(PROJECT_REVNUM, getRevisionNum());
        json.put(PROJECT_REFID, getReferenceId());
        if (getEndDate() != null)
            json.put(PROJECT_ENDDATE, getEndDate());

        if (getProjectItems() != null)
        {
            JSONArray jsonProjectItems = new JSONArray();
            for (ProjectItem projectItem : getProjectItems())
            {
                jsonProjectItems.put(projectItem.toJSON());
            }
            json.put(PROJECT_ITEMS, jsonProjectItems);
        }

        JSONArray extras = new JSONArray();
        Map<GWTPropertyDescriptor, Object> extraFields = getExtraFields();
        if(extraFields != null)
        {
            JSONObject jsonExtra;
            for (GWTPropertyDescriptor extraPd : extraFields.keySet())
            {
                jsonExtra = SNDService.get().convertPropertyDescriptorToJson(c, u, extraPd, true);
                jsonExtra.put("value", extraFields.get(extraPd));
                extras.put(jsonExtra);
            }

            json.put("extraFields", extras);
        }

        return json;
    }
}
