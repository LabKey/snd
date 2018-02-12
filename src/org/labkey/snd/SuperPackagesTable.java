/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.labkey.api.snd.SuperPackage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        hasDataSql.append("(CASE WHEN EXISTS (SELECT " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId FROM ");
        hasDataSql.append(SNDSchema.getInstance().getTableInfoEventData(), "ce");
        hasDataSql.append(" WHERE " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId = ce.SuperPkgId)");
        hasDataSql.append(" THEN 'true' ELSE 'false' END)");
        ExprColumn hasDataCol = new ExprColumn(this, "HasEvent", hasDataSql, JdbcType.BOOLEAN);
        addColumn(hasDataCol);

        SQLFragment inUseSql = new SQLFragment();
        inUseSql.append("(CASE WHEN EXISTS (SELECT " + ExprColumn.STR_TABLE_ALIAS + ".SuperPkgId FROM ");
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

    public boolean isPackageInUse(int superPkgId)
    {
        Set<String> cols = new HashSet<>();
        cols.add("HasEvent");
        cols.add("HasProject");
        TableSelector ts = new TableSelector(this, cols, new SimpleFilter(FieldKey.fromString("SuperPkgId"), superPkgId), null);
        Map<String, Object> ret = ts.getMap();

        return Boolean.parseBoolean((String) ret.get("HasEvent")) | Boolean.parseBoolean((String) ret.get("HasProject"));
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return new SuperPackagesTable.UpdateService(this);
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
            int superPkgId = (Integer) oldRowMap.get("SuperPkgId");
            if (isPackageInUse(superPkgId))
                throw new QueryUpdateServiceException("Package in use, cannot delete.");

            // if top-level super package
            if (oldRowMap.get("ParentSuperPkgId") == null)
            {
                // then delete all child super packages pointing to this top-level super package
                List<SuperPackage> childSuperPackages = SNDManager.getChildSuperPkgs(container, user, superPkgId);
                if (childSuperPackages != null)
                {
                    for (SuperPackage childSuperPackage : childSuperPackages)
                    {
                        Map<String, Object> superPackageRow = new HashMap<>(1);
                        superPackageRow.put("SuperPkgId", childSuperPackage.getSuperPkgId());
                        super.deleteRow(user, container, superPackageRow);
                    }
                }
            }

            return super.deleteRow(user, container, oldRowMap);
        }

        @Override
        protected Map<String, Object> getRow(User user, Container container, Map<String, Object> keys) throws InvalidKeyException, QueryUpdateServiceException, SQLException
        {
            Map<String, Object> row = super.getRow(user, container, keys);
            if(row == null)  // might have been deleted already due to package/super package cascading deletes
                return null;

            Set<String> cols = new HashSet<>();
            cols.add("HasEvent");
            cols.add("HasProject");
            TableSelector ts = new TableSelector(this.getQueryTable(), cols, new SimpleFilter(FieldKey.fromString("SuperPkgId"), row.get("SuperPkgId")), null);

            return row;
        }

    }
}
