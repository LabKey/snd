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
import org.labkey.api.dataiterator.DataIterator;
import org.labkey.api.dataiterator.DataIteratorBuilder;
import org.labkey.api.dataiterator.DataIteratorContext;
import org.labkey.api.dataiterator.ListofMapsDataIterator;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.ValidationException;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.snd.Event;
import org.labkey.api.snd.EventNote;
import org.labkey.api.snd.SNDService;
import org.labkey.snd.SNDManager;
import org.labkey.snd.SNDUserSchema;
import org.labkey.snd.security.permissions.SNDViewerPermission;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    protected class UpdateService extends SNDQueryUpdateService
    {
        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        private final SNDService _sndService = SNDService.get();
        private final SNDManager _sndManager = SNDManager.get();

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

                DataIteratorContext context = getDataIteratorContext(errors, QueryUpdateService.InsertOption.MERGE, configParameters);

                List<Map<String, Object>> rowsWithEventNoteId = rows.getDataIterator(context).stream().map(row -> {
                            EventNote eventNote = _sndManager.getEventNote(container, user, (int) row.get("eventId"));
                            if (eventNote != null) {
                                row.put("eventNoteId", eventNote.getEventNoteId());
                            }
                            return row;
                        }
                ).toList();

                Set<Integer> eventIds = rows.getDataIterator(context).stream()
                        .filter(row -> row.containsKey("eventId"))
                        .map(row -> (Integer) row.get("eventId"))
                        .collect(Collectors.toSet());

                Map<Boolean, List<Map<String, Object>>> partitionedMaps = rowsWithEventNoteId.stream().collect(Collectors.partitioningBy(row -> row.containsKey("eventNoteId") && row.get("eventNoteId") != null));

                List<Map<String, Object>> mapsWithEventNoteId = partitionedMaps.get(true);
                List<Map<String, Object>> mapsWithoutEventNoteId = partitionedMaps.get(false);

                DataIteratorBuilder iteratorWithEventNoteId;
                DataIteratorBuilder iteratorWithoutEventNoteId;

                if (!mapsWithoutEventNoteId.isEmpty()) {
                    iteratorWithoutEventNoteId = new ListofMapsDataIterator.Builder(mapsWithoutEventNoteId.get(0).keySet(), mapsWithoutEventNoteId);
                    result += super.importRows(user, container, iteratorWithoutEventNoteId, errors, configParameters, extraScriptContext);
                }
                if (!mapsWithEventNoteId.isEmpty()) {
                    iteratorWithEventNoteId = new ListofMapsDataIterator.Builder(mapsWithEventNoteId.get(0).keySet(), mapsWithEventNoteId);
                    result += _importRowsUsingDIB(user, container, iteratorWithEventNoteId, null, context, extraScriptContext);
                }

                _sndManager.updateNarrativeCache(container, user, eventIds, log);
            }
            return result;
        }

    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, SNDViewerPermission.class, getUserSchema().getContextualRoles());
    }
}
