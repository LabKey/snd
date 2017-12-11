package org.labkey.api.snd;

import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
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
    private int _chargeId;
    private Date _startDate;
    private Date  endDate;
    private String _description;
    private boolean _active;
    private int _refId;
    private List<ProjectItem> _projectItems;
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();

    public static final String PROJECT_ID = "ProjectId";
    public static final String PROJECT_DESCRIPTION = "Description";
    public static final String PROJECT_ACTIVE = "Active";
    public static final String PROJECT_STARTDATE = "StartDate";
    public static final String PROJECT_ENDDATE = "EndDate";
    public static final String PROJECT_OBJECTID = "ObjectId";
    public static final String PROJECT_CONTAINER = "Container";
    public static final String PROJECT_REVNUM = "RevisionNum";
    public static final String PROJECT_REFID = "ReferenceId";

    public Project (int id, Integer revNum, boolean edit, Container c)
    {
        _projectId = (edit ? id : SNDSequencer.PROJECTID.ensureId(c, id));
        _objectId = (edit ? null : GUID.makeGUID());
        _revisionNum = (edit ? revNum : 0);
    }

    public int getProjectId()
    {
        return _projectId;
    }

    public void setProjectId(int projectId)
    {
        _projectId = projectId;
    }

    public int getRefId()
    {
        return _refId;
    }

    public void setRefId(int refId)
    {
        _refId = refId;
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

    public int getChargeId()
    {
        return _chargeId;
    }

    public void setChargeId(int chargeId)
    {
        _chargeId = chargeId;
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
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
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
        projectValues.put(PROJECT_REFID, getRefId());
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
}
