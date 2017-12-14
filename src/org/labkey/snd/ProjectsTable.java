package org.labkey.snd;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProjectsTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public ProjectsTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new ProjectsTable.UpdateService(this);
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
            UserSchema schema = QueryService.get().getUserSchema(user, container, SNDSchema.NAME);
            TableInfo projectItemsTable = getTableInfo(schema, SNDSchema.PROJECTITEMS_TABLE_NAME);
            QueryUpdateService projectItemsQus = getQueryUpdateService(projectItemsTable);

            int projectId = (Integer) oldRowMap.get("ProjectId");
            int revNum = (Integer) oldRowMap.get("RevisionNum");

            List<Map<String, Object>> rows = SNDManager.get().getProjectItems(container, user, projectId, revNum);
            try
            {
                projectItemsQus.deleteRows(user, container, rows, null, null);
            }
            catch (BatchValidationException e)
            {
                throw new QueryUpdateServiceException(e);
            }

            // now delete package row
            return super.deleteRow(user, container, oldRowMap);
        }

        private TableInfo getTableInfo(@NotNull UserSchema schema, @NotNull String table)
        {
            TableInfo tableInfo = schema.getTable(table);
            if (tableInfo == null)
                throw new IllegalStateException(table + " TableInfo not found");

            return tableInfo;
        }

        private QueryUpdateService getQueryUpdateService(@NotNull TableInfo table)
        {
            QueryUpdateService qus = table.getUpdateService();
            if (qus == null)
                throw new IllegalStateException(table.getName() + " query update service");

            return qus;
        }

        @Override
        protected Map<String, Object> getRow(User user, Container container, Map<String, Object> keys) throws InvalidKeyException, QueryUpdateServiceException, SQLException
        {
            Map<String, Object> row = super.getRow(user, container, keys);

//            Set<String> cols = new HashSet<>();
//            cols.add("HasEvent");
//            cols.add("HasProject");
//            TableSelector ts = new TableSelector(this.getQueryTable(), cols, new SimpleFilter(FieldKey.fromString("PkgId"), row.get("PkgId")), null);
//            row.put(Package.PKG_HASEVENT, Boolean.parseBoolean((String) ts.getMap().get(Package.PKG_HASEVENT)));
//            row.put(Package.PKG_HASPROJECT, Boolean.parseBoolean((String) ts.getMap().get(Package.PKG_HASPROJECT)));

            return row;
        }

    }
}
