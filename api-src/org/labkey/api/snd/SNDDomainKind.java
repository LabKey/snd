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

import org.labkey.api.data.Container;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.query.ExtendedTableDomainKind;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by marty on 7/11/2017.
 */
public class SNDDomainKind extends ExtendedTableDomainKind
{
    private final String NAMESPACE_PREFIX = "snd";
    private final String SCHEMA_NAME = "snd";
    private final String KIND_NAME = "SND";


    @Override
    public boolean canCreateDefinition(User user, Container container)
    {
        return container.hasPermission("SNDDomainKind.canCreateDefinition", user, AdminPermission.class);
    }

    @Override
    protected String getSchemaName()
    {
        return SCHEMA_NAME;
    }

    @Override
    protected String getNamespacePrefix()
    {
        return NAMESPACE_PREFIX;
    }

    @Override
    public String getKindName()
    {
        return KIND_NAME;
    }

    @Override
    public Set<String> getReservedPropertyNames(Domain domain)
    {
        Set<String> result = new HashSet<>();
        result.add("Description");
        result.add("Active");
        result.add("ObjectId");
        result.add("QcState");
        result.add("Container");
        result.add("CreatedBy");
        result.add("Created");
        result.add("ModifiedBy");
        result.add("Modified");
        return result;
    }
}
