package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Event
{
    private Integer _eventId;
    private int _participantId;  // sometimes also a molecule ID
    private Date _date;
    private String _projectIdRev;
    private String _note;
    private Integer _noteId;
    private List<EventData> _eventData;
    private String _parentObjectId;
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();

    public static final String EVENT_ID = "eventId";
    public static final String EVENT_PARTICIPANT_ID = "participantId";
    public static final String EVENT_DATE = "date";
    public static final String EVENT_PROJECT_ID_REV = "projectIdRev";
    public static final String EVENT_NOTE = "note";
    public static final String EVENT_NOTE_ID = "EventNoteId";
    public static final String EVENT_DATA = "eventData";
    public static final String EVENT_PARENT_OBJECTID = "parentObjectId";

    public static final String dateFormat = "yyyy-MM-dd'T'kk:mm:ss";  // ISO8601
    public static final SimpleDateFormat dateFormatter;

    public static final String SND_EVENT_NAMESPACE = "SND.EventData";

    static {
        dateFormatter = new SimpleDateFormat(dateFormat);
    }

    public Event(Integer eventId, int participantId, Date date, String projectIdRev, String note, List<EventData> eventData, Container c)
    {
        _eventId = eventId != null ? eventId : SNDSequencer.EVENTID.ensureId(c, null);
        _participantId = participantId;
        _date = date;
        _projectIdRev = projectIdRev;
        _note = note;
        _eventData = eventData;
        _noteId = SNDSequencer.EVENTID.ensureId(c, null);
    }

    public Event () {}

    public Integer getEventId()
    {
        return _eventId;
    }

    public void setEventId(Integer eventId)
    {
        _eventId = eventId;
    }

    public int getParticipantId()
    {
        return _participantId;
    }

    public void setParticipantId(int participantId)
    {
        _participantId = participantId;
    }

    public Date getDate()
    {
        return _date;
    }

    public void setDate(Date date)
    {
        _date = date;
    }

    public String getProjectIdRev()
    {
        return _projectIdRev;
    }

    public void setProjectIdRev(String projectIdRev)
    {
        _projectIdRev = projectIdRev;
    }

    public String getParentObjectId()
    {
        return _parentObjectId;
    }

    public void setParentObjectId(String parentObjectId)
    {
        _parentObjectId = parentObjectId;
    }

    public String getNote()
    {
        return _note;
    }

    public void setNote(String note)
    {
        _note = note;
    }

    public Integer getNoteId()
    {
        return _noteId;
    }

    public void setNoteId(Integer noteId)
    {
        _noteId = noteId;
    }

    public List<EventData> getEventData()
    {
        return _eventData;
    }

    public void setEventData(List<EventData> eventData)
    {
        _eventData = eventData;
    }

    public Map<GWTPropertyDescriptor, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Map<GWTPropertyDescriptor, Object> extraFields)
    {
        _extraFields = extraFields;
    }

    public Map<String, Object> getEventRow(Container c)
    {
        Map<String, Object> eventValues = new ArrayListMap<>();
        if (getEventId() != null)
            eventValues.put(EVENT_ID, getEventId());

        eventValues.put(EVENT_PARTICIPANT_ID, getParticipantId());
        eventValues.put(EVENT_DATE, getDate());
        eventValues.put(EVENT_PARENT_OBJECTID, getParentObjectId());
        eventValues.put("Container", c);

        Map<GWTPropertyDescriptor, Object> extras = getExtraFields();
        for (GWTPropertyDescriptor gpd : extras.keySet())
        {
            eventValues.put(gpd.getName(), extras.get(gpd));
        }

        return eventValues;
    }

    public Map<String, Object> getEventNotesRow(Container c)
    {
        Map<String, Object> eventValues = new ArrayListMap<>();
        if (getEventId() != null)
            eventValues.put(EVENT_ID, getEventId());

        eventValues.put(EVENT_NOTE, getNote());
        eventValues.put(EVENT_NOTE_ID, getNoteId());
        eventValues.put("Container", c);

        return eventValues;
    }

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(EVENT_ID, getEventId());
        json.put(EVENT_PARTICIPANT_ID, getParticipantId());

        if (getDate() != null)
            json.put(EVENT_DATE, dateFormatter.format(getDate()));


        json.put(EVENT_PROJECT_ID_REV, getProjectIdRev());
        json.put(EVENT_NOTE, getNote());

        JSONArray eventDataJson = new JSONArray();
        if (getEventData() != null)
        {
            for (EventData eventData : getEventData())
            {
                eventDataJson.put(eventData.toJSON(c, u));
            }
        }
        json.put(EVENT_DATA, eventDataJson);

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
