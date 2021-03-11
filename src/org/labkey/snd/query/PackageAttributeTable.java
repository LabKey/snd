package org.labkey.snd.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.CoreSchema;
import org.labkey.api.data.ForeignKey;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.exp.OntologyManager;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.LookupForeignKey;
import org.labkey.api.query.QueryForeignKey;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.settings.AppProps;
import org.labkey.snd.SNDSchema;
import org.labkey.snd.SNDUserSchema;

public class PackageAttributeTable extends FilteredTable<SNDUserSchema>
{
    public PackageAttributeTable(@NotNull SNDUserSchema userSchema, ContainerFilter cf)
    {
        super(OntologyManager.getTinfoPropertyDescriptor(), userSchema, cf);
        setName(SNDUserSchema.TableType.PackageAttribute.name());
        setDescription("Package/attributes, one row per package/attribute combination.");

        ExprColumn pkgId = new ExprColumn(this, "pkgId", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgId"), JdbcType.INTEGER);
        addColumn(pkgId);

        ExprColumn pkgDescription = new ExprColumn(this, "pkgDescription", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgDescription"), JdbcType.VARCHAR);
        addColumn(pkgDescription);

        ExprColumn pkgActive = new ExprColumn(this, "pkgActive", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgActive"), JdbcType.BOOLEAN);
        addColumn(pkgActive);

        ExprColumn pkgRepeatable = new ExprColumn(this, "pkgRepeatable", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgRepeatable"), JdbcType.BOOLEAN);
        addColumn(pkgRepeatable);

        ExprColumn pkgQcState = new ExprColumn(this, "pkgQcState", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgQcState"), JdbcType.INTEGER);
        LookupForeignKey fk = new LookupForeignKey("RowId", "Label") {
            @Override
            public TableInfo getLookupTableInfo() {
                return CoreSchema.getInstance().getTableInfoUsers();
            }
        };
        pkgQcState.setFk(fk);
        addColumn(pkgQcState);

        ExprColumn pkgModified = new ExprColumn(this, "pkgModified", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgModified"), JdbcType.DATE);
        addColumn(pkgModified);

        ExprColumn pkgModifiedBy = new ExprColumn(this, "pkgModifiedBy", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgModifiedBy"), JdbcType.INTEGER);
        fk = new LookupForeignKey("UserId", "DisplayName") {
            @Override
            public TableInfo getLookupTableInfo() {
                return CoreSchema.getInstance().getTableInfoUsers();
            }
        };
        pkgModifiedBy.setFk(fk);
        addColumn(pkgModifiedBy);

        ExprColumn pkgCreated = new ExprColumn(this, "pkgCreated", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgCreated"), JdbcType.DATE);
        addColumn(pkgCreated);

        ExprColumn pkgCreatedBy = new ExprColumn(this, "pkgCreatedBy", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".pkgCreatedBy"), JdbcType.INTEGER);
        fk = new LookupForeignKey("UserId", "DisplayName") {
            @Override
            public TableInfo getLookupTableInfo() {
                return CoreSchema.getInstance().getTableInfoUsers();
            }
        };
        pkgCreatedBy.setFk(fk);
        addColumn(pkgCreatedBy);

        wrapAllColumns(true);

        ExprColumn validatorName = new ExprColumn(this, "validatorName", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".validatorName"), JdbcType.VARCHAR);
        addColumn(validatorName);

        ExprColumn validatorTypeURI = new ExprColumn(this, "validatorTypeURI", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".validatorTypeURI"), JdbcType.VARCHAR);
        addColumn(validatorTypeURI);

        ExprColumn validatorExpression = new ExprColumn(this, "validatorExpression", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".validatorExpression"), JdbcType.VARCHAR);
        addColumn(validatorExpression);

//
//        ExprColumn eventDataAndName = new ExprColumn(this, "EventDataAndName", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataAndName"), JdbcType.VARCHAR);
//        addColumn(eventDataAndName);
//
//        // Inject a lookup to the EventData table
//        ExprColumn eventDataCol = new ExprColumn(this, "EventData", new SQLFragment(ExprColumn.STR_TABLE_ALIAS + ".EventDataId"), JdbcType.VARCHAR);
//        addColumn(eventDataCol);
//        eventDataCol.setFk(QueryForeignKey.from(getUserSchema(), this.getContainerFilter())
//                .table(SNDUserSchema.TableType.EventData.name())
//                .key("EventDataId")
//                .display("EventDataId")
//                .raw(true));
//
//        // Inject a Container column directly into the table, making it easier to follow container filtering rules
//        ExprColumn containerCol = new ExprColumn(this, "Container", new SQLFragment(ExprColumn.STR_TABLE_ALIAS  + ".Container"), JdbcType.VARCHAR);
//        addColumn(containerCol);
//        containerCol.setFk(new ContainerForeignKey(getUserSchema()));
//
//        getMutableColumn("ObjectId").setFk((ForeignKey)null);
//        getMutableColumn("PropertyId").setLabel("Property");
    }

    @Override
    protected void applyContainerFilter(ContainerFilter filter)
    {
        // Handle this in the FROM SQL generation
    }

    @Override
    @NotNull
    public SQLFragment getFromSQL(String alias)
    {
        SQLFragment sql = new SQLFragment("(SELECT pkg.pkgId, pkg.Description as pkgDescription, pkg.Active as pkgActive, " +
                "pkg.Repeatable as pkgRepeatable, pkg.QcState as pkgQcState, pkg.Modified as pkgModified, pkg.Created as pkgCreated, " +
                "pkg.ModifiedBy as pkgModifiedBy, pkg.CreatedBy as pkgCreatedBy, " +
                "pd.*, pv.Name as validatorName, pv.TypeURI as validatorTypeURI, pv.Expression as validatorExpression FROM ");
        sql.append(SNDSchema.getInstance().getTableInfoPkgs(), "pkg");
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoDomainDescriptor(), "dd");
        sql.append(" ON dd.DomainURI LIKE CONCAT('%', 'snd', '%', 'Package-', pkg.PkgId) ");
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoPropertyDomain(), "pdom");
        sql.append(" ON pdom.DomainId = dd.DomainId ");
        sql.append(" INNER JOIN ");
        sql.append(OntologyManager.getTinfoPropertyDescriptor(), "pd");
        sql.append(" ON pd.PropertyId = pdom.PropertyId ");
        sql.append(" INNER JOIN ");
        sql.append(" exp.PropertyValidator pv");
        sql.append(" ON pd.PropertyId = pv.PropertyId ");
        sql.append(") ");
        sql.append(alias);

        return sql;
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, AdminPermission.class);
    }

}
