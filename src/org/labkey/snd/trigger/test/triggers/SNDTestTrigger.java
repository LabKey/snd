package org.labkey.snd.trigger.test.triggers;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.SuperPackage;

import java.util.List;

public class SNDTestTrigger implements EventDataTrigger
{
    @Override
    public void onInsert(Container c, User u, EventData eventData, Event event, List<SuperPackage> superPkgs, BatchValidationException errors)
    {

    }

    @Override
    public void onUpdate(Container c, User u, EventData eventData, Event event, List<SuperPackage> superPkgs, BatchValidationException errors)
    {

    }

    @Override
    public Integer getOrder()
    {
        return null;
    }

    @Override
    public void setOrder(Integer order)
    {

    }
}
