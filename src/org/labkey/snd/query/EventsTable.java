package org.labkey.snd.query;

import org.labkey.api.data.Container;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.snd.SNDService;
import org.labkey.snd.NarrativeAuditProvider;
import org.labkey.snd.SNDManager;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class EventsTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{
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
            String subjectId = (String) oldRowMap.get("SubjectId");
            Date eventDate = (Date) oldRowMap.get("Date");

            // This needs to be an atomic operation otherwise could get deadlock
            try (DbScope.Transaction tx = SNDSchema.getInstance().getSchema().getScope().ensureTransaction(SNDService.get().getWriteLock()))
            {
                SNDManager.get().deleteEventsCache(container, user, eventId);
                SNDManager.get().deleteEventDatas(container, user, eventId);
                SNDManager.get().deleteEventNotes(container, user, eventId);
                tx.commit();
            }
            catch (BatchValidationException e)
            {
                throw new QueryUpdateServiceException(e.getMessage());
            }

            NarrativeAuditProvider.addAuditEntry(container, user, eventId, subjectId, eventDate, null, "Delete event");

            // now delete package row
            return super.deleteRow(user, container, oldRowMap);
        }
    }

}
