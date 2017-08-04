package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.services.ServiceRegistry;

import java.util.List;

/**
 * Created by marty on 8/4/2017.
 */
public interface SNDService
{
    @Nullable
    static SNDService get()
    {
        return ServiceRegistry.get(SNDService.class);
    }

    List<String> savePackage(SNDPackage pkg);
}
