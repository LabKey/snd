package org.labkey.snd.trigger.test;

import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.EventDataTriggerFactory;
import org.labkey.snd.trigger.test.triggers.ChlorideBloodTestTrigger;
import org.labkey.snd.trigger.test.triggers.ChlorideTestTrigger;
import org.labkey.snd.trigger.test.triggers.ElectrolytesTestTrigger;
import org.labkey.snd.trigger.test.triggers.SNDTestTrigger;

public class SNDTestEventTriggerFactory implements EventDataTriggerFactory
{

    @Override
    public EventDataTrigger createTrigger(String category)
    {
        EventDataTrigger trigger;

        switch (category)
        {
            case "SNDTestTrigger":
                trigger = new SNDTestTrigger();
                break;
            case "ChlorideTestTrigger":
                trigger = new ChlorideTestTrigger();
                break;
            case "ChlorideBloodTestTrigger":
                trigger = new ChlorideBloodTestTrigger();
                break;
            case "ElectrolytesTestTrigger":
                trigger = new ElectrolytesTestTrigger();
                break;
            default:
                trigger = null;
        }

        return trigger;
    }
}
