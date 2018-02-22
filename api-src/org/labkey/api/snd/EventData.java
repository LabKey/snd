package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class EventData
{
    private Integer _eventDataId;
    private int _superPkgId;
    private int _eventId;
    private Integer _parentEventDataId;
    private String _narrative;
    private String _objectURI;
    private List<EventData> _subPackages;
    private List<AttributeData> _attributes = new ArrayList<>();
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();

    public static final String EVENT_DATA_ID = "eventDataId";
    public static final String EVENT_DATA_SUPER_PACKAGE_ID = "superPkgId";
    public static final String EVENT_DATA_NARRATIVE = "narrative";
    public static final String EVENT_DATA_SUB_PACKAGES = "subPackages";
    public static final String EVENT_DATA_ATTRIBUTES = "attributes";
    public static final String EVENT_DATA_OBJECTURI = "objectURI";
    public static final String EVENT_DATA_EVENTID = "eventId";
    public static final String EVENT_DATA_PARENT_EVENTDATAID = "parentEventDataId";


    public EventData(Integer eventDataId, int superPkgId, String narrative, List<EventData> subPackages, List<AttributeData> attributes)
    {
        _eventDataId = eventDataId;
        _superPkgId = superPkgId;
        _narrative = narrative;
        _subPackages = subPackages;

        if (attributes != null)
            _attributes = attributes;
    }

    public EventData() {}

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

    public Integer getParentEventDataId()
    {
        return _parentEventDataId;
    }

    public void setParentEventDataId(Integer parentEventDataId)
    {
        _parentEventDataId = parentEventDataId;
    }

    public String getNarrative()
    {
        return _narrative;
    }

    public void setNarrative(String narrative)
    {
        _narrative = narrative;
    }

    public String getObjectURI()
    {
        return _objectURI;
    }

    public void setObjectURI(String objectURI)
    {
        _objectURI = objectURI;
    }

    public int getEventId()
    {
        return _eventId;
    }

    public void setEventId(int eventId)
    {
        _eventId = eventId;
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

    public Map<GWTPropertyDescriptor, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Map<GWTPropertyDescriptor, Object> extraFields)
    {
        _extraFields = extraFields;
    }

    public Map<String, Object> getEventDataRow(Container c)
    {
        Map<String, Object> eventDataValues = new ArrayListMap<>();
        eventDataValues.put(EVENT_DATA_ID, getEventDataId());
        eventDataValues.put(EVENT_DATA_SUPER_PACKAGE_ID, getSuperPkgId());
        eventDataValues.put(EVENT_DATA_OBJECTURI, getObjectURI());
        eventDataValues.put(EVENT_DATA_EVENTID, getEventId());
        eventDataValues.put(EVENT_DATA_PARENT_EVENTDATAID, getParentEventDataId());
        eventDataValues.put("Container", c);

        Map<GWTPropertyDescriptor, Object> extras = getExtraFields();
        for (GWTPropertyDescriptor gpd : extras.keySet())
        {
            eventDataValues.put(gpd.getName(), extras.get(gpd));
        }

        return eventDataValues;
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
        json.put(EVENT_DATA_SUB_PACKAGES, subPackagesJson);

        JSONArray attributesJson = new JSONArray();
        if (getAttributes() != null)
        {
            for (AttributeData attributeData : getAttributes())
            {
                attributesJson.put(attributeData.toJSON(c, u));
            }
        }
        json.put(EVENT_DATA_ATTRIBUTES, attributesJson);

        JSONArray extras = new JSONArray();
        Map<GWTPropertyDescriptor, Object> extraFields = getExtraFields();
        if(extraFields != null)
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
