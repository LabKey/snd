package org.labkey.snd.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.snd.SNDUserSchema;
import org.labkey.snd.table.PlainTextNarrativeDisplayColumn;

import java.util.ArrayList;
import java.util.List;

public class EventsCacheTable extends SimpleUserSchema.SimpleTable<SNDUserSchema>
{
    /**
     * Create the simple table.
     * SimpleTable doesn't add columns until .init() has been called to allow derived classes to fully initialize themselves before adding columns.
     *
     * @param schema
     * @param table
     */
    public EventsCacheTable(SNDUserSchema schema, TableInfo table)
    {
        super(schema, table);
    }

    static final List<FieldKey> defaultVisibleColumns = new ArrayList<>();

    static {
        defaultVisibleColumns.add(FieldKey.fromParts("EventId"));
        defaultVisibleColumns.add(FieldKey.fromParts("HtmlNarrative"));
        defaultVisibleColumns.add(FieldKey.fromParts("Plain Text Narrative"));
    }

    @Override
    public boolean hasPermission(@NotNull UserPrincipal user, @NotNull Class<? extends Permission> perm)
    {
        return getContainer().hasPermission(user, AdminPermission.class);
    }

    @Override
    public List<FieldKey> getDefaultVisibleColumns()
    {
        return defaultVisibleColumns;
    }

    @Override
    public EventsCacheTable init()
    {
        super.init();

        ColumnInfo plainTextNarrativeColumn = addColumn(wrapColumn("Plain Text Narrative", getRealTable().getColumn("HtmlNarrative")));
        plainTextNarrativeColumn.setDisplayColumnFactory(new DisplayColumnFactory()
        {
            public DisplayColumn createRenderer(ColumnInfo colInfo)
            {
                return new PlainTextNarrativeDisplayColumn(colInfo);
            }
        });

        return this;
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        return null;
    }
}
