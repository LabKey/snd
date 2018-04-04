package org.labkey.snd.trigger.test.triggers;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.TriggerAction;

import java.util.Map;

public class ChlorideBloodTestTrigger implements EventDataTrigger
{
    final String name = "Chloride Blood Test Trigger";
    final String msgPrefix = name + ": ";


    @Override
    public void onInsert(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext)
    {
        TriggerHelper.ensureTriggerOrder(triggerAction.getIncomingEvent(), name, extraContext);
    }

    @Override
    public void onUpdate(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext)
    {
        onInsert(c, u, triggerAction, extraContext);
    }

    @Override
    public Integer getOrder()
    {
        return 2;
    }
}
