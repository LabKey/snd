package org.labkey.snd.trigger;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.module.Module;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.EventDataTriggerFactory;
import org.labkey.api.snd.SuperPackage;
import org.labkey.api.snd.TriggerAction;
import org.labkey.api.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class SNDTriggerManager
{
    private static final SNDTriggerManager _instance = new SNDTriggerManager();

    public static SNDTriggerManager get()
    {
        return _instance;
    }

    private Map<Module, EventDataTriggerFactory> _eventTriggerFactories = new HashMap<>();

    /**
     * Called from SNDService to allow event trigger factories to be registered.  These will be queried for category
     * triggers during insert and update events
     */
    public void registerEventTriggerFactory(Module module, EventDataTriggerFactory factory)
    {
        _eventTriggerFactories.put(module, factory);
    }

    public void unregisterEventTriggerFactory(Module module)
    {
        _eventTriggerFactories.remove(module);
    }

    private List<EventDataTriggerFactory> getTriggerFactories(Container c)
    {
        List<EventDataTriggerFactory> factories = new ArrayList<>();

        for (Module module : _eventTriggerFactories.keySet())
        {
            if (c.getActiveModules().contains(module))
            {
                factories.add(_eventTriggerFactories.get(module));
            }
        }

        return factories;
    }

    private SuperPackage getSuperPackage(int superPkgId, List<SuperPackage> superPkgs)
    {
        for (SuperPackage superPkg : superPkgs)
        {
            if (superPkg.getSuperPkgId() == superPkgId)
            {
                return superPkg;
            }
        }

        return null;
    }

    private List<TriggerAction> getCategoryTriggers(@NotNull Event event, @NotNull EventData eventData, SuperPackage superPackage,
                                                    @NotNull List<SuperPackage> topLevelSuperPackages, List<EventDataTriggerFactory> factories)
    {
        List<TriggerAction> triggers = new ArrayList<>();
        EventDataTrigger trigger;

        if (superPackage.getPkg() != null)
        {
            for (String category : superPackage.getPkg().getCategories().values())
            {
                for (EventDataTriggerFactory factory : factories)
                {
                    trigger = factory.createTrigger(category);
                    if (trigger != null)
                    {
                        triggers.add(new TriggerAction(trigger, event, eventData, superPackage, topLevelSuperPackages));
                    }
                }
            }
        }

        return triggers.stream().sorted((t1, t2) -> {
            if (t1.getTrigger().getOrder() != null && t1.getTrigger().getOrder() != null)
            {
                return t2.getTrigger().getOrder() - t1.getTrigger().getOrder();
            }

            return 1;
        }).collect(Collectors.toList());
    }

    private List<TriggerAction> getTriggerActions(Event event, List<SuperPackage> superPackages, List<EventDataTriggerFactory> factories)
    {
        List<TriggerAction> triggerActions = new ArrayList<>();
        List<TriggerAction> pkgTriggers;
        Queue<Pair<EventData, SuperPackage>> queue = new ConcurrentLinkedQueue<>();

        Pair<EventData, SuperPackage> pair;

        // iterate through top level packages, perform breadth first search on each top level event data
        for (EventData eventData : event.getEventData())
        {
            pkgTriggers = new ArrayList<>();
            SuperPackage superPackage = getSuperPackage(eventData.getSuperPkgId(), superPackages);
            queue.add(new Pair<>(eventData, superPackage));

            while (!queue.isEmpty())
            {
                pair = queue.poll();
                pkgTriggers.addAll(getCategoryTriggers(event, pair.first, pair.second, superPackages, factories));

                if (pair.first.getSubPackages() != null)
                {
                    for (EventData data : pair.first.getSubPackages())
                    {
                        queue.add(new Pair<>(data, getSuperPackage(data.getSuperPkgId(), pair.second.getChildPackages())));
                    }
                }
            }

            // reverse bfs
            triggerActions.addAll(Lists.reverse(pkgTriggers));
        }

        return triggerActions;
    }


    /**
     * Called from insert event.
     */
    public void fireInsertTriggers(Container c, User u, Event event, List<SuperPackage> topLevelPkgs, BatchValidationException errors)
    {
        List<EventDataTriggerFactory> factories = getTriggerFactories(c);

        // Nothing to do
        if (factories.size() < 1)
            return;

        List<TriggerAction> triggerActions = getTriggerActions(event, topLevelPkgs, factories);
        Map<String, Object> extraContext = new HashMap<>();

        for (TriggerAction triggerAction : triggerActions)
        {
            triggerAction.getTrigger().onInsert(c, u, triggerAction, errors, extraContext);
        }
    }

    /**
     * Called from update event.
     */
    public void fireUpdateTriggers(Container c, User u, Event event, List<SuperPackage> topLevelPkgs, BatchValidationException errors)
    {
        List<EventDataTriggerFactory> factories = getTriggerFactories(c);

        // Nothing to do
        if (factories.size() < 1)
            return;

        List<TriggerAction> triggers = getTriggerActions(event, topLevelPkgs, factories);
        Map<String, Object> extraContext = new HashMap<>();

        for (TriggerAction trigger : triggers)
        {
            trigger.getTrigger().onUpdate(c, u, trigger, errors, extraContext);
        }
    }
}
