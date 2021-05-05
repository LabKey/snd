/*
 * Copyright (c) 2018-2019 LabKey Corporation
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
package org.labkey.snd.query;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.dataiterator.DataIteratorBuilder;
import org.labkey.api.dataiterator.DataIteratorContext;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.snd.SNDService;
import org.labkey.snd.SNDManager;
import org.labkey.snd.SNDUserSchema;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EventNotesTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{
    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public EventNotesTable(SNDUserSchema schema, TableInfo table, ContainerFilter cf)
    {
        super(schema, table, cf);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new EventNotesTable.UpdateService(this);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        private final SNDService _sndService = SNDService.get();

        private int getRowCount(DataIteratorBuilder rows, @Nullable Map<Enum,Object> configParameters, BatchValidationException errors)
        {
            List<Map<String, Object>> data;
            int rowCount = 0;

            DataIteratorContext dataIteratorContext = getDataIteratorContext(errors, QueryUpdateService.InsertOption.MERGE, configParameters);

            try
            {
                data = _sndService.getMutableData(rows, dataIteratorContext);
                rowCount = data.size();
            }
            catch (IOException e)
            {
                errors.addRowError(new ValidationException(e.getMessage()));
            }

            return rowCount;
        }

        @Override
        public int loadRows(User user, Container container, DataIteratorBuilder rows, DataIteratorContext context, @Nullable Map<String, Object> extraScriptContext)
        {
            if (context.getInsertOption() == QueryUpdateService.InsertOption.MERGE || context.getInsertOption() == QueryUpdateService.InsertOption.REPLACE)
            {
                return mergeRows(user, container, rows, context.getErrors(), context.getConfigParameters(), extraScriptContext);
            }
            else
            {
                return importRows(user, container, rows, context.getErrors(), context.getConfigParameters(), extraScriptContext);
            }
        }

        @Override
        public int mergeRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                              @Nullable Map<Enum,Object> configParameters, Map<String, Object> extraScriptContext)
        {
            Logger log = SNDManager.getLogger(configParameters, EventNotesTable.class);
            // Large merge triggers importRows path
            int result = 0;
            if (getRowCount(rows, configParameters, errors) > SNDManager.MAX_MERGE_ROWS)
            {
                log.info("More than " + SNDManager.MAX_MERGE_ROWS + " rows. using importRows method.");
                result = super.importRows(user, container, rows, errors, configParameters, extraScriptContext);
            }
            else
            {
                log.info("Merging rows.");
                result = super.mergeRows(user, container, rows, errors, configParameters, extraScriptContext);
            }
            return result;
        }

    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, AdminPermission.class);
    }
}
