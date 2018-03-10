package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class EventsTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{
    private static final ReentrantLock eventLock = new ReentrantLock();

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public EventsTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new EventsTable.UpdateService(this);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        @Override
        protected Map<String, Object> deleteRow(User user, Container container, Map<String, Object> oldRowMap) throws QueryUpdateServiceException, SQLException, InvalidKeyException
        {
            int eventId = (Integer) oldRowMap.get("EventId");

            // This needs to be an atomic operation otherwise could get deadlock
            try (DbScope.Transaction tx = QueryService.get().getUserSchema(user, container, SNDSchema.NAME).getDbSchema().getScope().ensureTransaction(eventLock))
            {
                SNDManager.get().deleteEventDatas(container, user, eventId);
                SNDManager.get().deleteEventNotes(container, user, eventId);
                tx.commit();
            }
            catch (BatchValidationException e)
            {
                throw new QueryUpdateServiceException(e.getMessage());
            }

            // now delete package row
            return super.deleteRow(user, container, oldRowMap);
        }
    }

}
