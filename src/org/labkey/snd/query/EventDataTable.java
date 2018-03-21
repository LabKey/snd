package org.labkey.snd.query;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
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
            List<Map<String, Object>> data = null;
            DataIteratorContext dataIteratorContext = getDataIteratorContext(errors, InsertOption.MERGE, configParameters);
            try
            {
                data = _sndService.getMutableData(rows, dataIteratorContext);
            }
            catch (IOException e)
            {
                return 0;
            }

            _logger.info("Begin updating exp.Object table.");
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
                    _logger.info("Updated " + count + " rows in exp.Object table.");
            }
            _logger.info("End updating exp.Object table. Updated total of " + count + " rows.");

            //TODO : data.get(0) -- NPE
            DataIteratorBuilder rowsWithObjectURI = new ListofMapsDataIterator.Builder(data.get(0).keySet(), data);
            return _importRowsUsingDIB(user, container, rowsWithObjectURI, null, dataIteratorContext, extraScriptContext);
        }

        @Override
        public int importRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                              @Nullable Map<Enum,Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            List<Map<String, Object>> data = null;

            try
            {
                data = _sndService.getMutableData(rows, getDataIteratorContext(errors, InsertOption.IMPORT, configParameters));
            }
            catch (IOException e)
            {
                return 0;
            }

            _logger.info("Begin inserting into exp.Object.");
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
                    _logger.info("Inserted " + count + " rows in exp.Object table.");
            }
            _logger.info("End inserting into exp.Object. Inserted total of " + count + " rows.");

            DataIteratorBuilder rowsWithObjectURI = new ListofMapsDataIterator.Builder(data.get(0).keySet(), data);

            //insert into snd.EventData (which includes extensible columns for EventData)
            return super.importRows(user, container, rowsWithObjectURI, errors, configParameters, extraScriptContext);
        }

        @Override
        public List<Map<String, Object>> deleteRows(User user, Container container, List<Map<String, Object>> oldRows,
                                                    @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws InvalidKeyException, BatchValidationException, QueryUpdateServiceException, SQLException
        {

            _logger.info("Begin deleting from exp.ObjectProperty and exp.Object.");
            int count = 0;

            //This will be a cascading delete across exp.ObjectProperty, exp.Object, and snd.EventData
            for (Map<String, Object> map : oldRows)
            {
                String objectURI = getObjectURI((Integer) map.get("EventDataId"), container);
                OntologyObject obj = OntologyManager.getOntologyObject(container, objectURI);

                //delete row from exp.ObjectProperty
                OntologyManager.deleteProperties(container, obj.getObjectId());

                //delete row from exp.Object
                OntologyManager.deleteOntologyObjects(container, objectURI);

                count++;
                //TODO: Count in exp.Object is not going to be the same as in snd.EventData - need to figure out how to get the count to log
                if (count % 10 == 0)
                    _logger.info("Deleted " + count + " rows from exp.ObjectProperty and exp.Object.");
            }

            _logger.info("End deleting from exp.ObjectProperty and exp.Object. Deleted total of " + count + " rows.");

            return super.deleteRows(user, container, oldRows, configParameters, extraScriptContext);
        }
    }
}
