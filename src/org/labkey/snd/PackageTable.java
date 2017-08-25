package org.labkey.snd;

import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.SimpleUserSchema;

/**
 * Created by marty on 8/23/2017.
 */
public class PackageTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public PackageTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public SimpleUserSchema.SimpleTable init()
    {
        super.init();

        SQLFragment inUseSql = new SQLFragment();
        inUseSql.append("(CASE WHEN EXISTS (SELECT sp.PkgId FROM ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoSuperPkgs(), "sp");
        inUseSql.append(" JOIN ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoCodedEvents(), "ce");
        inUseSql.append(" ON sp.SuperPkgId = ce.SuperPkgId");
        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".PkgId = sp.PkgId)");
        inUseSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn inUseCol = new ExprColumn(this, "hasData", inUseSql, JdbcType.BOOLEAN);
        addColumn(inUseCol);

        return this;
    }


}
