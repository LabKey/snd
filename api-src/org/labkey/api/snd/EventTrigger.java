package org.labkey.api.snd;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;

import java.util.List;

public interface EventTrigger
{
    void onInsert(Container c, User u, EventData eventData, Event event, List<Package> pkgs, BatchValidationException errors);
    void onUpdate(Container c, User u, EventData eventData, Event event, List<Package> pkgs, BatchValidationException errors);
    Integer getOrder();
    void setOrder(Integer order);
}
