package org.labkey.snd.query;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.dataiterator.DataIteratorBuilder;
import org.labkey.api.dataiterator.DataIteratorContext;
import org.labkey.api.dataiterator.ListofMapsDataIterator;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.exp.OntologyObject;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.snd.SNDService;
import org.labkey.snd.SNDManager;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EventDataTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{
    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public EventDataTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new EventDataTable.UpdateService(this);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        private final SNDManager _sndManager = SNDManager.get();
        private final SNDService _sndService = SNDService.get();
        private final Logger _logger = Logger.getLogger(EventDataTable.class);

        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        private String getObjectURI(Integer eventDataId, Container c)
        {
            return _sndManager.generateLsid(c, String.valueOf(eventDataId));
        }

        @Override
        public int mergeRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                             @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            List<Map<String, Object>> data;
            DataIteratorContext dataIteratorContext = getDataIteratorContext(errors, InsertOption.MERGE, configParameters);
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            try
            {
                data = _sndService.getMutableData(rows, dataIteratorContext);
            }
            catch (IOException e)
            {
                return 0;
            }

            log.info("Begin updating exp.Object table.");
            int count = 0;
            for(Map<String, Object> map : data)
            {
                String objectURI = getObjectURI((Integer) map.get("EventDataId"), container);

                //update snd.EventData row with objectURI
                map.put("ObjectURI", objectURI);

                //delete row from exp.Object
                OntologyManager.deleteOntologyObjects(container, objectURI);

                //add updated row to exp.Object
                OntologyManager.ensureObject(container, objectURI);

                count++;
                //TODO: Count in exp.Object is not going to be the same as in snd.EventData - need to figure out how to get the count to log
                if(count % 1000 == 0)
                    log.info("Updated " + count + " rows in exp.Object table.");
            }
            log.info("End updating exp.Object table. Updated total of " + count + " rows.");

            if(data.size() > 0 && null != data.get(0))
            {
                DataIteratorBuilder rowsWithObjectURI = new ListofMapsDataIterator.Builder(data.get(0).keySet(), data);
                return _importRowsUsingDIB(user, container, rowsWithObjectURI, null, dataIteratorContext, extraScriptContext);
            }

            return 0; //there aren't any rows to merge
        }

        @Override
        public int importRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                              @Nullable Map<Enum,Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            List<Map<String, Object>> data;
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            try
            {
                data = _sndService.getMutableData(rows, getDataIteratorContext(errors, InsertOption.IMPORT, configParameters));
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
                return 0;
            }

            log.info("Begin inserting into exp.Object.");
            int count = 0;
            for(Map<String, Object> map : data)
            {
                String objectURI = getObjectURI((Integer) map.get("EventDataId"), container);

                //update snd.EventData row with objectURI
                map.put("ObjectURI", objectURI);

                //add new row to exp.Object
                OntologyManager.ensureObject(container, objectURI);

                count++;
                //TODO: Count in exp.Object is not going to be the same as in snd.EventData - need to figure out how to get the count to log
                if(count % 1000 == 0)
                    log.info("Inserted " + count + " rows in exp.Object table.");
            }
            log.info("End inserting into exp.Object. Inserted total of " + count + " rows.");

            DataIteratorBuilder rowsWithObjectURI = new ListofMapsDataIterator.Builder(data.get(0).keySet(), data);

            //insert into snd.EventData (which includes extensible columns for EventData)
            return super.importRows(user, container, rowsWithObjectURI, errors, configParameters, extraScriptContext);
        }

        @Override
        public List<Map<String, Object>> deleteRows(User user, Container container, List<Map<String, Object>> oldRows,
                                                    @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws InvalidKeyException, BatchValidationException, QueryUpdateServiceException, SQLException
        {
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            deleteFromExpTables(oldRows, container, log);
            return super.deleteRows(user, container, oldRows, configParameters, extraScriptContext);
        }

        @Override
        public int truncateRows(User user, Container container, @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws BatchValidationException, QueryUpdateServiceException, SQLException
        {
            Logger log = null;
            if (configParameters != null)
            {
                log = ((Logger) configParameters.get(QueryUpdateService.ConfigParameters.Logger));
            }

            if (log == null)
            {
                log = _logger;
            }

            //get rows from snd.eventData
            TableSelector ts = new TableSelector(SNDSchema.getInstance().getTableInfoEventData());
            List<Map<String, Object>> oldRows = (List<Map<String, Object>>) ts.getMapCollection();
            deleteFromExpTables(oldRows, container, log);

            return super.truncateRows(user, container, configParameters, extraScriptContext);
        }

        private void deleteFromExpTables(List<Map<String, Object>> oldRows, Container container, Logger log)
        {
            log.info("Begin deleting from exp.ObjectProperty and exp.Object.");
            int count = 0;

            //This will be a cascading delete across exp.ObjectProperty, exp.Object, and snd.EventData
            for (Map<String, Object> map : oldRows)
            {
                String objectURI = getObjectURI((Integer) map.get("EventDataId"), container);
                OntologyObject obj = OntologyManager.getOntologyObject(container, objectURI);

                //delete row from exp.ObjectProperty
                if(null != obj)
                    OntologyManager.deleteProperties(container, obj.getObjectId());

                //delete row from exp.Object
                OntologyManager.deleteOntologyObjects(container, objectURI);

                count++;
                //TODO: Count in exp.Object is not going to be the same as in snd.EventData - need to figure out how to get the count to log
                if (count % 1000 == 0)
                    log.info("Deleted " + count + " rows from exp.ObjectProperty and exp.Object.");
            }

            log.info("End deleting from exp.ObjectProperty and exp.Object. Deleted total of " + count + " rows.");
        }
    }
}