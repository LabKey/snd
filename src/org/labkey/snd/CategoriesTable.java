package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by marty on 8/27/2017.
 */
public class CategoriesTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public CategoriesTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public SimpleUserSchema.SimpleTable init()
    {
        super.init();

        SQLFragment inUseSql = new SQLFragment();
        inUseSql.append("(CASE WHEN EXISTS (SELECT PkgId FROM ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoPkgCategoryJunction(), "pcj");
        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".CategoryId = pcj.CategoryId)");
        inUseSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn inUseCol = new ExprColumn(this, "InUse", inUseSql, JdbcType.BOOLEAN);
        addColumn(inUseCol);

        return this;
    }

    protected boolean isCategoryInUse(Container c, User u, int catId)
    {
        TableSelector ts = new TableSelector(this, Collections.singleton("InUse"), new SimpleFilter(FieldKey.fromString("PkgId"), catId), null);
        Boolean[] ret = ts.getArray(Boolean.class);

        return ret[0];
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new CategoriesTable.UpdateService(this);
    }

    protected class UpdateService extends SimpleQueryUpdateService
    {
        public UpdateService(SimpleUserSchema.SimpleTable ti)
        {
            super(ti, ti.getRealTable());
        }

        public UpdateService(SimpleUserSchema.SimpleTable simpleTable, TableInfo table, DomainUpdateHelper helper)
        {
            super(simpleTable, table, helper);
        }

        @Override
        protected Map<String, Object> deleteRow(User user, Container container, Map<String, Object> oldRowMap) throws QueryUpdateServiceException, SQLException, InvalidKeyException
        {
            int categoryId = (Integer) oldRowMap.get("CategoryId");
            if (isCategoryInUse(container, user, categoryId))
                throw new QueryUpdateServiceException("Category in use, cannot delete.");

            return super.deleteRow(user, container, oldRowMap);
        }

    }
}
