package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;

public interface EventDataTriggerFactory
{
    @Nullable EventDataTrigger createTrigger(String category);
}
