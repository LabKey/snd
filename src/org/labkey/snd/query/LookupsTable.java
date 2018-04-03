package org.labkey.snd.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.dataiterator.DataIteratorBuilder;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.snd.SNDManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LookupsTable extends SimpleUserSchema.SimpleTable
{
    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public LookupsTable(UserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new LookupsTable.UpdateService(this);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        @Override
        public int mergeRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                             @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            int result = super.mergeRows(user, container, rows, errors, configParameters, extraScriptContext);
            SNDManager.get().getCache().clear();
            return result;
        }

        @Override
        public int importRows(User user, Container container, DataIteratorBuilder rows, BatchValidationException errors,
                              @Nullable Map<Enum, Object> configParameters, Map<String, Object> extraScriptContext) throws SQLException
        {
            int result = super.importRows(user, container, rows, errors, configParameters, extraScriptContext);
            SNDManager.get().getCache().clear();
            return result;
        }

        @Override
        public List<Map<String, Object>> deleteRows(User user, Container container, List<Map<String, Object>> oldRows,
                                                    @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws InvalidKeyException, BatchValidationException, QueryUpdateServiceException, SQLException
        {
            List<Map<String, Object>> result = super.deleteRows(user, container, oldRows, configParameters, extraScriptContext);
            SNDManager.get().getCache().clear();
            return result;
        }

        @Override
        public int truncateRows(User user, Container container, @Nullable Map<Enum, Object> configParameters, @Nullable Map<String, Object> extraScriptContext)
                throws BatchValidationException, QueryUpdateServiceException, SQLException
        {
            int result = super.truncateRows(user, container, configParameters, extraScriptContext);
            SNDManager.get().getCache().clear();
            return result;
        }
    }
}
