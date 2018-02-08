package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.util.List;
import java.util.Map;

public class EventData
{
    private Integer _eventDataId;
    private int _superPkgId;
    private String _narrative;
    private List<EventData> _subPackages;
    private List<AttributeData> _attributes;

    public static final String EVENT_DATA_ID = "eventDataId";
    public static final String EVENT_DATA_SUPER_PACKAGE_ID = "superPkgId";
    public static final String EVENT_DATA_NARRATIVE = "narrative";
    public static final String EVENT_DATA_SUB_PACKAGES = "subPackages";
    public static final String EVENT_DATA_ATTRIBUTES = "attributes";


    public EventData(Integer eventDataId, int superPkgId, String narrative, List<EventData> subPackages, List<AttributeData> attributes)
    {
        _eventDataId = eventDataId;
        _superPkgId = superPkgId;
        _narrative = narrative;
        _subPackages = subPackages;
        _attributes = attributes;
    }

    public Integer getEventDataId()
    {
        return _eventDataId;
    }

    public void setEventDataId(Integer eventDataId)
    {
        _eventDataId = eventDataId;
    }

    public int getSuperPkgId()
    {
        return _superPkgId;
    }

    public void setSuperPkgId(int superPkgId)
    {
        _superPkgId = superPkgId;
    }

    public String getNarrative()
    {
        return _narrative;
    }

    public void setNarrative(String narrative)
    {
        _narrative = narrative;
    }

    public List<EventData> getSubPackages()
    {
        return _subPackages;
    }

    public void setSubPackages(List<EventData> subPackages)
    {
        _subPackages = subPackages;
    }

    public List<AttributeData> getAttributes()
    {
        return _attributes;
    }

    public void setAttributes(List<AttributeData> attributes)
    {
        _attributes = attributes;
    }

    public Map<String, Object> getEventDataRow()
    {
        Map<String, Object> attributeDataValues = new ArrayListMap<>();
        attributeDataValues.put(EVENT_DATA_ID, getEventDataId());
        attributeDataValues.put(EVENT_DATA_SUPER_PACKAGE_ID, getSuperPkgId());
        attributeDataValues.put(EVENT_DATA_SUB_PACKAGES, getSubPackages());
        attributeDataValues.put(EVENT_DATA_ATTRIBUTES, getAttributes());

        return attributeDataValues;
    }

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(EVENT_DATA_ID, getEventDataId());
        json.put(EVENT_DATA_SUPER_PACKAGE_ID, getSuperPkgId());
        json.put(EVENT_DATA_NARRATIVE, getNarrative());

        JSONArray subPackagesJson = new JSONArray();
        if (getSubPackages() != null)
        {
            for (EventData eventData : getSubPackages())
            {
                subPackagesJson.put(eventData.toJSON(c, u));
            }
        }

        JSONArray attributesJson = new JSONArray();
        if (getAttributes() != null)
        {
            for (AttributeData attributeData : getAttributes())
            {
                attributesJson.put(attributeData.toJSON(c, u));
            }
        }

        return json;
    }
}
