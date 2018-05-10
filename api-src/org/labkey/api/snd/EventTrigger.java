package org.labkey.api.snd;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;

import java.util.List;
import java.util.Map;

public interface EventTrigger
{
    void onInsert(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext);
    void onUpdate(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext);
    default Integer getOrder()
    {
        return null;
    }
}
