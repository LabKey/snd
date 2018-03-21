package org.labkey.snd.trigger.test.triggers;

import org.labkey.api.data.Container;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.snd.EventData;
import org.labkey.api.snd.EventDataTrigger;
import org.labkey.api.snd.TriggerAction;
import org.labkey.api.snd.Package;

import java.util.Map;


public class ChlorideTestTrigger implements EventDataTrigger
{
    final String name = "Chloride Test Trigger";
    final String msgPrefix = name + ": ";

    private void ensureAmountForUnits(EventData eventData, Package pkg, BatchValidationException errors)
    {
        String amountValue = TriggerHelper.getAttributeValue("amount", eventData, pkg, msgPrefix, errors);
        String unitsValue = TriggerHelper.getAttributeValue("units", eventData, pkg, msgPrefix, errors);
        Double newAmountValue;

        if (!errors.hasErrors() && unitsValue != null && amountValue != null)
        {
            if (!unitsValue.equals("mEq/L") && !unitsValue.equals("mg/dL"))
            {
                errors.addRowError(new ValidationException(msgPrefix + "Invalid units (" + unitsValue + "). mEq/L or mg/dL required."));
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
    public void onInsert(Container c, User u, TriggerAction triggerAction, BatchValidationException errors, Map<String, Object> extraContext)
    {
        ensureAmountForUnits(triggerAction.getIncomingEventData(), triggerAction.getSuperPackage().getPkg(), errors);
        TriggerHelper.ensureTriggerOrder(name, errors, extraContext);
    }

    @Override
    public void onUpdate(Container c, User u, TriggerAction triggerAction, BatchValidationException errors, Map<String, Object> extraContext)
    {
        onInsert(c, u, triggerAction, errors, extraContext);
    }

    @Override
    public Integer getOrder()
    {
        return 1;
    }
}
