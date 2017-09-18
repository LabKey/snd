package org.labkey.snd;

import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.SimpleUserSchema;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by marty on 8/23/2017.
 */
public class SuperPackagesTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{

    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public SuperPackagesTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    @Override
    public SimpleUserSchema.SimpleTable init()
    {
        super.init();

        SQLFragment hasDataSql = new SQLFragment();
        hasDataSql.append("(CASE WHEN EXISTS (SELECT " + ExprColumn.STR_TABLE_ALIAS + ".PkgId FROM ");
        hasDataSql.append(SNDSchema.getInstance().getTableInfoCodedEvents(), "ce");
        hasDataSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId = ce.SuperPkgId)");
        hasDataSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn hasDataCol = new ExprColumn(this, "HasEvent", hasDataSql, JdbcType.BOOLEAN);
        addColumn(hasDataCol);

        SQLFragment inUseSql = new SQLFragment();
        inUseSql.append("(CASE WHEN EXISTS (SELECT " + ExprColumn.STR_TABLE_ALIAS + ".PkgId FROM ");
        inUseSql.append(SNDSchema.getInstance().getTableInfoProjectItems(), "pi");
        inUseSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId = pi.SuperPkgId)");
        inUseSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn inUseCol = new ExprColumn(this, "HasProject", inUseSql, JdbcType.BOOLEAN);
        addColumn(inUseCol);

        SQLFragment isPrimitiveSql = new SQLFragment();
        isPrimitiveSql.append("(CASE WHEN NOT EXISTS (SELECT sp.ParentSuperPkgId FROM ");
        isPrimitiveSql.append(SNDSchema.getInstance().getTableInfoSuperPkgs(), "sp");
        isPrimitiveSql.append(" WHERE sp.ParentSuperPkgId = " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId)");
        isPrimitiveSql.append(" AND " + ExprColumn.STR_TABLE_ALIAS + ".ParentSuperPkgId IS NULL");
        isPrimitiveSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn isPrimitiveCol = new ExprColumn(this, "IsPrimitive", isPrimitiveSql, JdbcType.BOOLEAN);
        isPrimitiveCol.setDescription("A super package with a null ParentSuperPkgId and is not a parent of any other super package.");
        addColumn(isPrimitiveCol);

        return this;
    }

    public boolean isPackageInUse(int pkgId)
    {
        Set<String> cols = new HashSet<>();
        cols.add("HasEvent");
        cols.add("HasProject");
        TableSelector ts = new TableSelector(this, cols, new SimpleFilter(FieldKey.fromString("PkgId"), pkgId), null);
        Map<String, Object> ret = ts.getMap();

        return Boolean.parseBoolean((String) ret.get("HasEvent")) | Boolean.parseBoolean((String) ret.get("HasProject"));
    }
}
