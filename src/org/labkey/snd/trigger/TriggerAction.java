package org.labkey.snd.trigger;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.SuperPackage;

import java.util.List;

public class TriggerAction
{
    EventDataTrigger _trigger;
    Event _event;
    EventData _eventData;
    List<SuperPackage> _topLevelPkgs;

    public TriggerAction(EventDataTrigger trigger, @NotNull Event event, @NotNull EventData eventData, @NotNull List<SuperPackage> topLevelPkgs)
    {
        _trigger = trigger;
        _event = event;
        _eventData = eventData;
        _topLevelPkgs = topLevelPkgs;
    }

    public EventDataTrigger getTrigger()
    {
        return _trigger;
    }

    public void setTrigger(EventDataTrigger trigger)
    {
        _trigger = trigger;
    }

    public Event getEvent()
    {
        return _event;
    }

    public void setEvent(Event event)
    {
        _event = event;
    }

    public EventData getEventData()
    {
        return _eventData;
    }

    public void setEventData(EventData eventData)
    {
        _eventData = eventData;
    }

    public List<SuperPackage> getTopLevelPkgs()
    {
        return _topLevelPkgs;
    }

    public void setTopLevelPkgs(List<SuperPackage> topLevelPkgs)
    {
        _topLevelPkgs = topLevelPkgs;
    }
}
