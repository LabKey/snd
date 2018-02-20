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
package org.labkey.snd;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.collections.CaseInsensitiveTreeSet;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


public class SNDUserSchema extends SimpleUserSchema
{
    public SNDUserSchema(String name, @Nullable String description, User user, Container container, DbSchema dbschema)
    {
        super(name, description, user, container, dbschema);
    }

    public enum TableType
    {
        SuperPkgs
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new SuperPackagesTable(schema, SNDSchema.getInstance().getTableInfoSuperPkgs()).init();
                    }
                },
        Pkgs
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new PackagesTable(schema, SNDSchema.getInstance().getTableInfoPkgs()).init();
                    }
                },
        PkgCategories
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new CategoriesTable(schema, SNDSchema.getInstance().getTableInfoPkgCategories()).init();

                    }
                },
        PkgCategoryJunction
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoPkgCategoryJunction()).init();

                        return table;
                    }
                },
        ProjectItems
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoProjectItems()).init();

                        return table;
                    }
                },
        Projects
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new ProjectsTable(schema, SNDSchema.getInstance().getTableInfoProjects()).init();
                    }
                },
        Events
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new EventsTable(schema, SNDSchema.getInstance().getTableInfoEvents()).init();
                    }
                },
        EventNotes
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new EventNotesTable(schema, SNDSchema.getInstance().getTableInfoEventNotes()).init();
                    }
                },
        EventData
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        return new EventDataTable(schema, SNDSchema.getInstance().getTableInfoEventData()).init();
                    }
                },
        Lookups
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoLookups()).init();

                        return table;
                    }
                },
        LookupSets
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoLookupSets()).init();

                        return table;
                    }
                };


        public abstract TableInfo createTable(SNDUserSchema schema);
    }

    @Override
    @Nullable
    public TableInfo createTable(String name)
    {
        if (name != null)
        {
            TableType tableType = null;
            for (TableType t : TableType.values())
            {
                // Make the enum name lookup case insensitive
                if (t.name().equalsIgnoreCase(name.toLowerCase()))
                {
                    tableType = t;
                    break;
                }
            }
            if (tableType != null)
            {
                return tableType.createTable(this);
            }
            else
            {
                Map<String, Map<String, Object>> nameMap = getLookupSets();
                if (nameMap.containsKey(name))
                {
                    TableInfo table = SNDSchema.getInstance().getTableInfoLookups();
                    return new LookupSetTable(this, table, name, nameMap.get(name)).init();
                }
            }
        }
        return null;
    }

    public Map<String, Map<String, Object>> getLookupSets()
    {
        Map<String, Map<String, Object>> nameMap = (Map<String, Map<String, Object>>) SNDManager.get().getCache().get(LookupSetTable.getCacheKey(getContainer()));
        if (nameMap != null)
            return nameMap;

        nameMap = new CaseInsensitiveHashMap<>();

        TableSelector ts = new TableSelector(SNDSchema.getInstance().getTableInfoLookupSets(), new SimpleFilter(FieldKey.fromString("container"), getContainer().getId()), null);
        Map<String, Object>[] rows = ts.getMapArray();
        if (rows.length > 0)
        {
            Set<String> existing = super.getTableNames();
            for (Map<String, Object> row : rows)
            {
                String setname = (String)row.get("SetName");
                if (setname != null && !existing.contains(setname))
                    nameMap.put(setname, row);
            }
        }

        nameMap = Collections.unmodifiableMap(nameMap);
        SNDManager.get().getCache().put(LookupSetTable.getCacheKey(getContainer()), nameMap);

        return nameMap;
    }

    @Override
    public Set<String> getTableNames()
    {
        Set<String> tables = new CaseInsensitiveTreeSet();
        tables.addAll(getLookupSets().keySet());
        tables.addAll(super.getTableNames());

        return tables;
    }

    @Override
    public synchronized Set<String> getVisibleTableNames()
    {
        Set<String> tables = new CaseInsensitiveTreeSet();
        tables.addAll(super.getVisibleTableNames());
        tables.addAll(getLookupSets().keySet());

        return tables;
    }
}