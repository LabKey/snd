package org.labkey.api.snd;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TriggerAction
{
    EventDataTrigger _trigger;
    Event _event;
    EventData _eventData;
    List<SuperPackage> _topLevelPkgs;
    SuperPackage _superPackage;

    public TriggerAction(@NotNull EventDataTrigger trigger, @NotNull Event event, @NotNull EventData eventData,
                         @NotNull SuperPackage superPackage, @NotNull List<SuperPackage> topLevelPkgs)
    {
        _trigger = trigger;
        _event = event;
        _eventData = eventData;
        _topLevelPkgs = topLevelPkgs;
        _superPackage = superPackage;
    }

    public EventDataTrigger getTrigger()
    {
        return _trigger;
    }

    public void setTrigger(EventDataTrigger trigger)
    {
        _trigger = trigger;
    }

    public Event getIncomingEvent()
    {
        return _event;
    }

    public void setEvent(Event event)
    {
        _event = event;
    }

    public EventData getIncomingEventData()
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

    public SuperPackage getSuperPackage()
    {
        return _superPackage;
    }

    public void setSuperPackage(SuperPackage superPackage)
    {
        _superPackage = superPackage;
    }
}