/*
 * Copyright (c) 2017 LabKey Corporation
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
package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;
import org.labkey.api.services.ServiceRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

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
    void savePackage(Container c, User u, Package pkg, SuperPackage superPkg, boolean cloneFlag);
    void saveSuperPackages(Container c, User u, List<SuperPackage> superPkgs);
    List<Package> getPackages(Container c, User u, List<Integer> pkgIds, boolean includeExtraFields, boolean includeLookups, boolean includeAllAttributes);
    void registerAttributeLookup(Container c, User u, String schema, @Nullable String table);
    Map<String, String> getAttributeLookups(Container c, User u);
    Object getDefaultLookupDisplayValue(User u, Container c, String schema, String table, Object key);
    void saveProject(Container c, User u, Project project, boolean isRevision);
    Project getProject(Container c, User u, int projectId, int revNum);
    void saveEvent(Container c, User u, Event event);
    Event getEvent(Container c, User u, int eventId);
    JSONObject convertPropertyDescriptorToJson(Container c, User u, GWTPropertyDescriptor pd, boolean resolveLookupValues);
    JSONArray lookupValuesToJson(Container c, User u, String schema, String query);
    Lock getWriteLock();
}
