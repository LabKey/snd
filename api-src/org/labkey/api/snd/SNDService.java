package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.services.ServiceRegistry;

import java.util.List;
import java.util.Map;

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

    void savePackage(Container c, User u, Package pkg);
    void saveSuperPackages(Container c, User u, List<SuperPackage> superPkgs);
    List<Package> getPackages(Container c, User u, List<Integer> pkgIds);
    void registerAttributeLookup(Container c, User u, String schema, @Nullable String table);
    Map<String, String> getAttributeLookups(Container c, User u);
}
