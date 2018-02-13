package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;
import org.labkey.api.util.DateUtil;
import org.labkey.api.util.GUID;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Project
{
    private int _projectId = -1;
    private int _revisionNum;
    private String _objectId;
    private Date _startDate;
    private Date  _endDate;
    private String _description;
    private boolean _active;
    private int _referenceId;
    private boolean _hasEvent;
    private boolean _copyRevisedPkgs;
    private Date _endDateRevised;
    private List<ProjectItem> _projectItems = new ArrayList<>();
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

    public void updateObjectId(String objectId)
    {
        setObjectId(objectId);
        for (ProjectItem projectItem : _projectItems)
        {
            projectItem.setParentObjectId(objectId);
        }
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

    public String startDateToString()
    {
        return DateUtil.formatDateISO8601(getStartDate());
    }

    public void setStartDate(Date startDate)
    {
        _startDate = startDate;
    }

    public Date getEndDate()
    {
        return _endDate;
    }

    public String endDateToString()
    {
        return DateUtil.formatDateISO8601(getEndDate());
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

    public boolean hasEvent()
    {
        return _hasEvent;
    }

    public void setHasEvent(boolean hasEvent)
    {
        _hasEvent = hasEvent;
    }

    public boolean isCopyRevisedPkgs()
    {
        return _copyRevisedPkgs;
    }

    public void setCopyRevisedPkgs(boolean copyRevisedPkgs)
    {
        _copyRevisedPkgs = copyRevisedPkgs;
    }

    public Date getEndDateRevised()
    {
        return _endDateRevised;
    }

    public void setEndDateRevised(Date endDateRevised)
    {
        _endDateRevised = endDateRevised;
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
        json.put(PROJECT_STARTDATE, startDateToString());
        json.put(PROJECT_REVNUM, getRevisionNum());
        json.put(PROJECT_REFID, getReferenceId());
        json.put(PROJECT_HASEVENT, hasEvent());
        if (getEndDate() != null)
            json.put(PROJECT_ENDDATE, endDateToString());

        if (getProjectItems().size() > 0)
        {
            JSONArray jsonProjectItems = new JSONArray();
            for (ProjectItem projectItem : getProjectItems())
            {
                jsonProjectItems.put(projectItem.toJSON(c, u));
            }
            json.put(PROJECT_ITEMS, jsonProjectItems);
        }

        JSONArray extras = new JSONArray();
        Map<GWTPropertyDescriptor, Object> extraFields = getExtraFields();
        if(extraFields.size() > 0)
        {
            JSONObject jsonExtra;
            Set<GWTPropertyDescriptor> keys = new TreeSet<>(
                    Comparator.comparing(GWTPropertyDescriptor::getName)
            );
            keys.addAll(extraFields.keySet());
            for (GWTPropertyDescriptor extraPd : keys)
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
