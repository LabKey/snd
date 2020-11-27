package org.labkey.snd.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.settings.AppProps;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

public class DataByCategoryTable extends FilteredTable<SNDUserSchema>
{
    public DataByCategoryTable(@NotNull SNDUserSchema userSchema, ContainerFilter cf)
    {
        super(SNDSchema.getInstance().getTableInfoMvAttributesCategories(), userSchema, cf);
        setName(SNDUserSchema.TableType.DataByCategory.name());
        setDescription("Indexed view containing all mocked up dataset data.");

        wrapAllColumns(true);

        ExprColumn lsid = new ExprColumn(this, "lsid", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".lsid"), JdbcType.VARCHAR);
        addColumn(lsid);

        ExprColumn eventDataCol = new ExprColumn(this, "EventData", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataId"), JdbcType.VARCHAR);
        addColumn(eventDataCol);
        eventDataCol.setFk(QueryForeignKey.from(getUserSchema(), this.getContainerFilter())
                .table(SNDUserSchema.TableType.EventData.name())
                .key("EventDataId")
                .display("EventDataId")
                .raw(true));

        ExprColumn eventCol = new ExprColumn(this, "Event", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventId"), JdbcType.VARCHAR);
        addColumn(eventCol);
        eventCol.setFk(QueryForeignKey.from(getUserSchema(), this.getContainerFilter())
                .table(SNDUserSchema.TableType.Events.name())
                .key("EventId")
                .display("EventId")
                .raw(true));
    }

    @Override
    public @NotNull SQLFragment getFromSQL(String alias)
    {
        // LSID similar to what would be on a dataset
        String mockLsidPrefix = "'urn:lsid:" + AppProps.getInstance().getDefaultLsidAuthority() + ":Study.Data-'";
        String mockLsid = getRealTable().getSqlDialect().concatenate(mockLsidPrefix,
                "CAST(ObjectId AS VARCHAR) as lsid");

        // Requires NOEXPAND to ensure SQl SERVER does not try to use the view as a regular view
        SQLFragment sql = new SQLFragment("(SELECT *, " + mockLsid + " FROM snd.mv_dataByCategory WITH (NOEXPAND)) ");
        sql.append(alias);

        return sql;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, AdminPermission.class);
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        // Handle this in the FROM SQL generation
    }
}
