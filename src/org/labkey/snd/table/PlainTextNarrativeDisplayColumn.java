package org.labkey.snd.table;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.RenderContext;

public class PlainTextNarrativeDisplayColumn extends DataColumn
{
    public PlainTextNarrativeDisplayColumn(ColumnInfo col)
    {
        super(col);
    }

    @Override
    public String getFormattedValue(RenderContext ctx)
    {
        String htmlNarrative = (String)ctx.get(getColumnInfo().getFieldKey());
        String textNarrative;
        if (htmlNarrative != null && !htmlNarrative.isEmpty())
            textNarrative = htmlNarrative.replaceAll("\\<.*?\\>", "");  // crudely remove all HTML tags, or things that look like them
        else
            textNarrative = "";

        return textNarrative;
    }
}
