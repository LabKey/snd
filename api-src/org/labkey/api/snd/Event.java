package org.labkey.api.snd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;
import org.labkey.api.util.DateUtil;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.labkey.api.snd.EventNarrativeOption.HTML_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.REDACTED_HTML_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.REDACTED_TEXT_NARRATIVE;
import static org.labkey.api.snd.EventNarrativeOption.TEXT_NARRATIVE;

/**
 * Class for event data and related methods. Used when saving, updating, deleting and getting an event
 */
public class Event
{
    private Integer _eventId;
    private String _subjectId;
    private Date _date;
    private String _projectIdRev;
    private String _note;
    private Integer _noteId;
    private List<EventData> _eventData;
    private String _parentObjectId;
    private Map<EventNarrativeOption, String> narratives;
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();

    public static final String EVENT_ID = "eventId";
    public static final String EVENT_SUBJECT_ID = "subjectId";
    public static final String EVENT_DATE = "date";
    public static final String EVENT_PROJECT_ID_REV = "projectIdRev";
    public static final String EVENT_NOTE = "note";
    public static final String EVENT_DATA = "eventData";
    public static final String EVENT_PARENT_OBJECTID = "parentObjectId";
    public static final String EVENT_CONTAINER = "Container";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'kk:mm:ss";  // ISO8601 w/24-hour time and 'T' character

    public static final String SND_EVENT_NAMESPACE = "SND.EventData";

    public static final String SND_EVENT_DATE_CSS_CLASS = "snd-event-date";
    public static final String SND_EVENT_SUBJECT_CSS_CLASS = "snd-event-subject";


    public Event(@Nullable Integer eventId, String subjectId, @Nullable Date date, @NotNull String projectIdRev, @Nullable String note, @Nullable List<EventData> eventData, @NotNull Container c)
    {
        _eventId = eventId != null ? eventId : SNDSequencer.EVENTID.ensureId(c, null);
        _subjectId = subjectId;
        _date = date;
        _projectIdRev = projectIdRev;
        _note = note;
        _eventData = eventData;
        _noteId = SNDSequencer.EVENTID.ensureId(c, null);
    }

    public Event () {}

    @Nullable
    public Integer getEventId()
    {
        return _eventId;
    }

    public void setEventId(Integer eventId)
    {
        _eventId = eventId;
    }

    public String getSubjectId()
    {
        return _subjectId;
    }

    public void setSubjectId(String subjectId)
    {
        _subjectId = subjectId;
    }

    @Nullable
    public Date getDate()
    {
        return _date;
    }

    public void setDate(Date date)
    {
        _date = date;
    }

    @Nullable
    public String getProjectIdRev()
    {
        return _projectIdRev;
    }

    public void setProjectIdRev(String projectIdRev)
    {
        _projectIdRev = projectIdRev;
    }

    @Nullable
    public String getParentObjectId()
    {
        return _parentObjectId;
    }

    public void setParentObjectId(String parentObjectId)
    {
        _parentObjectId = parentObjectId;
    }

    @Nullable
    public String getNote()
    {
        return _note;
    }

    public void setNote(String note)
    {
        _note = note;
    }

    @Nullable
    public Integer getNoteId()
    {
        return _noteId;
    }

    public void setNoteId(Integer noteId)
    {
        _noteId = noteId;
    }

    @Nullable
    public List<EventData> getEventData()
    {
        return _eventData;
    }

    public void setEventData(List<EventData> eventData)
    {
        _eventData = eventData;
    }

    @NotNull
    public Map<GWTPropertyDescriptor, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(@NotNull Map<GWTPropertyDescriptor, Object> extraFields)
    {
        _extraFields = extraFields;
    }

    public Map<EventNarrativeOption, String> getNarratives()
    {
        return narratives;
    }

    public void setNarratives(Map<EventNarrativeOption, String> narratives)
    {
        this.narratives = narratives;
    }

    @NotNull
    public Map<String, Object> getEventRow(Container c)
    {
        Map<String, Object> eventValues = new ArrayListMap<>();
        if (getEventId() != null)
            eventValues.put(EVENT_ID, getEventId());

        eventValues.put(EVENT_SUBJECT_ID, getSubjectId());
        eventValues.put(EVENT_DATE, getDate());
        eventValues.put(EVENT_PARENT_OBJECTID, getParentObjectId());
        eventValues.put(EVENT_CONTAINER, c.getId());

        Map<GWTPropertyDescriptor, Object> extras = getExtraFields();
        for (GWTPropertyDescriptor gpd : extras.keySet())
        {
            eventValues.put(gpd.getName(), extras.get(gpd));
        }

        return eventValues;
    }

    @NotNull
    public Map<String, Object> getEventNotesRow(Container c)
    {
        Map<String, Object> eventValues = new ArrayListMap<>();
        if (getEventId() != null)
            eventValues.put(EVENT_ID, getEventId());

        eventValues.put(EVENT_NOTE, getNote());
        eventValues.put(EVENT_CONTAINER, c.getId());

        return eventValues;
    }

    @NotNull
    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(EVENT_ID, getEventId());
        json.put(EVENT_SUBJECT_ID, getSubjectId());

        if (getDate() != null)
            json.put(EVENT_DATE, DateUtil.formatDateTime(getDate(), DATE_TIME_FORMAT));

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
        if (extraFields != null)
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

        Map<EventNarrativeOption, String> narratives = getNarratives();

        for(EventNarrativeOption narrativeOption : narratives.keySet())
        {
            String narrative = narratives.get(narrativeOption);
            switch (narrativeOption)
            {
                case TEXT_NARRATIVE:
                    json.put(TEXT_NARRATIVE.getKey(), narrative);
                    break;
                case REDACTED_TEXT_NARRATIVE:
                    json.put(REDACTED_TEXT_NARRATIVE.getKey(), narrative);
                    break;
                case HTML_NARRATIVE:
                    json.put(HTML_NARRATIVE.getKey(), narrative);
                    break;
                case REDACTED_HTML_NARRATIVE:
                    json.put(REDACTED_HTML_NARRATIVE.getKey(), narrative);
                    break;
            }
        }

        return json;
    }
}
