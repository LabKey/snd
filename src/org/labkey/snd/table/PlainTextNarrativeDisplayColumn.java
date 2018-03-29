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
        return removeHtmlTagsFromNarrative(htmlNarrative);
    }

   public static String removeHtmlTagsFromNarrative(String htmlNarrative)
   {
       String textNarrative;

       if (htmlNarrative != null && !htmlNarrative.isEmpty())
       {
           // first crudely change these three tags to newlines
           textNarrative = htmlNarrative.replace("<br>", "\n");
           textNarrative = textNarrative.replace("<div class='snd-event-data'>", "\n");
           textNarrative = textNarrative.replace("<div class='snd-event-subject'>", "\n");
           // then crudely remove all other HTML open/close tags, or things that look like them
           textNarrative = textNarrative.replaceAll("\\<.*?\\>", "");
       }
       else
           textNarrative = "";

       return textNarrative;
   }
}
