/*
 * Copyright (c) 2018 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.snd.trigger.test.triggers;

import org.labkey.api.data.Container;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.AttributeData;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventTrigger;
import org.labkey.api.snd.TriggerAction;
import org.labkey.api.snd.Package;

import java.util.Map;


public class ChlorideTestTrigger implements EventTrigger
{
    final String name = "Chloride Test Trigger";
    final String msgPrefix = name + ": ";

    private void ensureAmountForUnits(Event event, EventData eventData, Package pkg)
    {
        AttributeData amountAttribute = TriggerHelper.getAttribute("amount", eventData, pkg);
        AttributeData unitsAttribute = TriggerHelper.getAttribute("units", eventData, pkg);
        String amountValue = amountAttribute.getValue();
        String unitsValue = unitsAttribute.getValue();
        Double newAmountValue;

        if (unitsValue != null && amountValue != null)
        {
            if (!unitsValue.equals("mEq/L") && !unitsValue.equals("mg/dL"))
            {
                unitsAttribute.setException(event, new ValidationException(msgPrefix + "Invalid units (" + unitsValue + "). mEq/L or mg/dL required.",
                        (unitsAttribute.getPropertyName() != null ? unitsAttribute.getPropertyName() : Integer.toString(unitsAttribute.getPropertyId()))));
            }

            if (unitsValue.equals("mg/dL"))
            {
                // convert to mEq/L: mg/dL * g/L * valence / atomic weight
                newAmountValue = Double.parseDouble(amountValue) * 10 * 1 / 35.5;
                TriggerHelper.setAttributeValue(eventData, TriggerHelper.getPropertyId("amount", pkg), "amount", Double.toString(newAmountValue));
                TriggerHelper.setAttributeValue(eventData, TriggerHelper.getPropertyId("units", pkg), "units", "mEq/L");
            }
        }
    }

    @Override
    public void onInsert(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext)
    {
        ensureAmountForUnits(triggerAction.getEvent(), triggerAction.getEventData(), triggerAction.getSuperPackage().getPkg());
        TriggerHelper.ensureTriggerOrder(triggerAction.getEvent(), name, extraContext);
    }

    @Override
    public void onUpdate(Container c, User u, TriggerAction triggerAction, Map<String, Object> extraContext)
    {
        onInsert(c, u, triggerAction, extraContext);
    }

    @Override
    public Integer getOrder()
    {
        return 1;
    }
}
