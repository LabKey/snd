package org.labkey.snd.trigger.test;

import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.EventDataTriggerFactory;
import org.labkey.snd.trigger.test.triggers.SNDTestTrigger;

public class SNDTestEventTriggerFactory implements EventDataTriggerFactory
{

    @Override
    public EventDataTrigger createTrigger(String category)
    {
        EventDataTrigger trigger;

        switch (category)
        {
            case "SNDTestTriggerCategory":
                trigger = new SNDTestTrigger();
                break;
            default:
                trigger = null;
        }

        return trigger;
    }
}
