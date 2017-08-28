package org.labkey.snd;

import org.labkey.api.data.Container;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.exp.DomainNotFoundException;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.SimpleQueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.snd.PackageDomainKind;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by marty on 8/23/2017.
 */
public class PackagesTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public PackagesTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public SimpleUserSchema.SimpleTable init()
    {
        super.init();

        SQLFragment hasDataSql = new SQLFragment();
        hasDataSql.append("(CASE WHEN EXISTS (SELECT sp.PkgId FROM ");
        hasDataSql.append(SNDSchema.getInstance().getTableInfoSuperPkgs(), "sp");
        hasDataSql.append(" JOIN ");
        hasDataSql.append(SNDSchema.getInstance().getTableInfoCodedEvents(), "ce");
        hasDataSql.append(" ON sp.SuperPkgId = ce.SuperPkgId");
        hasDataSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".PkgId = sp.PkgId)");
        hasDataSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn hasDataCol = new ExprColumn(this, "HasEvent", hasDataSql, JdbcType.BOOLEAN);
        addColumn(hasDataCol);

        SQLFragment inUseSql = new SQLFragment();
        inUseSql.append("(CASE WHEN EXISTS (SELECT sp.PkgId FROM ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoSuperPkgs(), "sp");
        inUseSql.append(" JOIN ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoProjectItems(), "pi");
        inUseSql.append(" ON sp.SuperPkgId = pi.SuperPkgId");
        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".PkgId = sp.PkgId)");
        inUseSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn inUseCol = new ExprColumn(this, "HasProject", inUseSql, JdbcType.BOOLEAN);
        addColumn(inUseCol);

        return this;
    }

    protected boolean isPackageInUse(Container c, User u, int pkgId)
    {
        Set<String> cols = new HashSet<>();
        cols.add("HasEvent");
        cols.add("HasProject");
        TableSelector ts = new TableSelector(this, cols, new SimpleFilter(FieldKey.fromString("PkgId"), pkgId), null);
        Map<String, Object> ret = ts.getMap();

        return Boolean.parseBoolean((String) ret.get("HasEvent")) | Boolean.parseBoolean((String) ret.get("HasProject"));
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new PackagesTable.UpdateService(this);
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
            int pkgId = (Integer) oldRowMap.get("PkgId");
            if (isPackageInUse(container, user, pkgId))
                throw new QueryUpdateServiceException("Package in use, cannot delete.");

            SNDManager.get().deletePackageCategories(container, user, pkgId);

            String domainName = SNDManager.getPackageName(pkgId);
            Domain domain = PropertyService.get().getDomain(getDomainContainer(container), PackageDomainKind.getDomainURI(SNDSchema.NAME, domainName, container, user));
            if (domain == null)
                throw new QueryUpdateServiceException("Package domain not found.");

            try
            {
                domain.delete(user);
            }
            catch (DomainNotFoundException e)
            {
                throw new QueryUpdateServiceException(e);
            }

            return super.deleteRow(user, container, oldRowMap);
        }

    }
}
