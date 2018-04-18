package org.labkey.api.snd;


import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TriggerAction
{
    EventTrigger _trigger;
    Event _event;
    EventData _eventData;
    Map<Integer, SuperPackage> _topLevelEventDataSuperPkgs;  // Maps EventDataId to SuperPackage
    SuperPackage _superPackage;

    public TriggerAction(@NotNull EventTrigger trigger, @NotNull Event event, @NotNull EventData eventData,
                         @NotNull SuperPackage superPackage, @NotNull Map<Integer, SuperPackage> topLevelPkgs)
    {
        _trigger = trigger;
        _event = event;
        _eventData = eventData;
        _topLevelEventDataSuperPkgs = topLevelPkgs;
        _superPackage = superPackage;
    }

    public EventTrigger getTrigger()
    {
        return _trigger;
    }

    public void setTrigger(EventTrigger trigger)
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

    public Map<Integer, SuperPackage> getTopLevelEventDataSuperPkgs()
    {
        return _topLevelEventDataSuperPkgs;
    }

    public void setTopLevelEventDataSuperPkgs(Map<Integer, SuperPackage> topLevelEventDataSuperPkgsPkgs)
    {
        _topLevelEventDataSuperPkgs = topLevelEventDataSuperPkgsPkgs;
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