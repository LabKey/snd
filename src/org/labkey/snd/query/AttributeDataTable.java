package org.labkey.snd.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.settings.AppProps;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

/**
 * Exposes all of the event attribute data, one row per attribute/value combination.
 * Essentially, a specialized and filtered wrapper over exp.ObjectProperty
 */
public class AttributeDataTable extends FilteredTable<SNDUserSchema>
{
    public AttributeDataTable(@NotNull SNDUserSchema userSchema)
    {
        super(OntologyManager.getTinfoObjectProperty(), userSchema);
        setName(SNDUserSchema.TableType.AttributeData.name());
        setDescription("Event/package attribute data, one row per attribute/value combination.");

        wrapAllColumns(true);

        ExprColumn objectURI = new ExprColumn(this, "ObjectURI", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".ObjectURI"), JdbcType.VARCHAR);
        addColumn(objectURI);

        // Inject a lookup to the EventData table
        ExprColumn eventDataCol = new ExprColumn(this, "EventData", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataId"), JdbcType.VARCHAR);
        addColumn(eventDataCol);
        eventDataCol.setFk(new QueryForeignKey(getUserSchema(), null, SNDUserSchema.TableType.EventData.name(), "EventDataId", "EventDataId", true));

        // Inject a Container column directly into the table, making it easier to follow container filtering rules
        ExprColumn containerCol = new ExprColumn(this, "Container", new SQLFragment(ExprColumn.STR_TABLE_ALIAS  + ".Container"), JdbcType.VARCHAR);
        addColumn(containerCol);
        containerCol.setFk(new ContainerForeignKey(getUserSchema()));

        getColumn("ObjectId").setFk(null);
        getColumn("PropertyId").setLabel("Property");
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        // Handle this in the FROM SQL generation
    }

    @NotNull
    public SQLFragment getFromSQL(String alias)
    {
        // Flatten data from primary base table (exp.ObjectProperty), exp.Object, and snd.EventData
        SQLFragment sql = new SQLFragment("(SELECT X.*, o.Container, o.ObjectURI, ed.EventDataId FROM ");
        sql.append(super.getFromSQL("X"));
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoObject(), "o");
        sql.append(" ON x.ObjectId = o.ObjectId AND ");
        // Apply the container filter
        sql.append(getContainerFilter().getSQLFragment(getSchema(), new SQLFragment("o.Container"), getContainer()));
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoPropertyDescriptor(), "pd");
        // Filter to include only properties associated with packages
        sql.append(" ON x.PropertyId = pd.PropertyId AND pd.PropertyURI LIKE ? INNER JOIN ");
        // Filter to include only values associated with EventDatas
        sql.append(SNDSchema.getInstance().getTableInfoEventData(), "ed");
        sql.append(" ON ed.ObjectURI = o.ObjectURI ");
        sql.append(") ");
        sql.append(alias);

        // Note - this must be kept in sync with the PropertyURIs generated for the packages
        sql.add("urn:lsid:" + AppProps.getInstance().getDefaultLsidAuthority() + ":package-snd.Folder-%");

        return sql;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        // Allow read access based on standard container permission, but for everything else require admin container access
        if (perm.equals(ReadPermission.class))
        {
            return getContainer().hasPermission(user, perm);
        }
        return getContainer().hasPermission(user, AdminPermission.class);
    }

    @Override
    public @Nullable QueryUpdateService getUpdateService()
    {
        // TODO - override for custom implementation
        return super.getUpdateService();
    }
}
