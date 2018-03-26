package org.labkey.api.snd;

public enum EventNarrativeOption
{
    TEXT_NARRATIVE("textNarrative"),
    REDACTED_TEXT_NARRATIVE("redactedTextNarrative"),
    HTML_NARRATIVE("htmlNarrative"),
    REDACTED_HTML_NARRATIVE("redactedHtmlNarrative");

    private String _key;

    EventNarrativeOption(String key)
    {
        _key = key;
    }

    public String getKey()
    {
        return _key;
    }
}
