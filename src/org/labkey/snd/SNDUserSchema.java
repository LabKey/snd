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
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;


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
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoSuperPkgs()).init();

                        return table;
                    }
                },
        Pkgs
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoPkgs()).init();


                        SQLFragment inUseSql = new SQLFragment();
                        inUseSql.append("(CASE WHEN EXISTS (SELECT sp.PkgId FROM ");
                        inUseSql.append(SNDSchema.getInstance().getTableInfoSuperPkgs(), "sp");
                        inUseSql.append(" JOIN ");
                        inUseSql.append(SNDSchema.getInstance().getTableInfoCodedEvents(), "ce");
                        inUseSql.append(" ON sp.SuperPkgId = ce.SuperPkgId");
                        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".PkgId = sp.PkgId)");
                        inUseSql.append(" THEN 'true' ELSE 'false' END)");
                        ExprColumn inUseCol = new ExprColumn(table, "InUse", inUseSql, JdbcType.BOOLEAN);
                        table.addColumn(inUseCol);

                        return table;
                    }
                },
        PkgCategories
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoPkgCategories()).init();

                        SQLFragment inUseSql = new SQLFragment();
                        inUseSql.append("(CASE WHEN EXISTS (SELECT PkgId FROM ");
                        inUseSql.append(SNDSchema.getInstance().getTableInfoPkgCategoryJunction(), "pcj");
                        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".CategoryId = pcj.CategoryId)");
                        inUseSql.append(" THEN 'true' ELSE 'false' END)");
                        ExprColumn inUseCol = new ExprColumn(table, "InUse", inUseSql, JdbcType.BOOLEAN);
                        table.addColumn(inUseCol);

                        return table;
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
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoProjects()).init();

                        return table;
                    }
                },
        Events
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoEvents()).init();

                        return table;
                    }
                },
        EventNotes
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoEventNotes()).init();

                        return table;
                    }
                },
        CodedEvents
                {
                    @Override
                    public TableInfo createTable(SNDUserSchema schema)
                    {
                        SimpleUserSchema.SimpleTable<SNDUserSchema> table =
                                new SimpleUserSchema.SimpleTable<>(
                                        schema, SNDSchema.getInstance().getTableInfoCodedEvents()).init();

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
        }
        return null;
    }
}