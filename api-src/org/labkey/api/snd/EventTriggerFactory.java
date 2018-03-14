package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;

public interface EventTriggerFactory
{
    @Nullable EventTrigger createTrigger(String category);
}
