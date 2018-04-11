package org.labkey.snd.trigger.test;

import org.labkey.api.snd.EventTrigger;
import org.labkey.api.snd.EventTriggerFactory;
import org.labkey.snd.trigger.test.triggers.CalciumTestTrigger;
import org.labkey.snd.trigger.test.triggers.ChlorideBloodTestTrigger;
import org.labkey.snd.trigger.test.triggers.ChlorideTestTrigger;
import org.labkey.snd.trigger.test.triggers.ElectrolytesTestTrigger;
import org.labkey.snd.trigger.test.triggers.SNDTestTrigger;

public class SNDTestEventTriggerFactory implements EventTriggerFactory
{

    @Override
    public EventTrigger createTrigger(String category)
    {
        EventTrigger trigger;

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
            case "CalciumTestTrigger":
                trigger = new CalciumTestTrigger();
                break;
            default:
                trigger = null;
        }

        return trigger;
    }
}
