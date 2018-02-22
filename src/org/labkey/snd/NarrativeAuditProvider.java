package org.labkey.snd;

import org.labkey.api.audit.AbstractAuditTypeProvider;
import org.labkey.api.audit.AuditLogService;
import org.labkey.api.audit.AuditTypeEvent;
import org.labkey.api.audit.AuditTypeProvider;
import org.labkey.api.audit.query.AbstractAuditDomainKind;
import org.labkey.api.data.Container;
import org.labkey.api.exp.PropertyDescriptor;
import org.labkey.api.exp.PropertyType;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NarrativeAuditProvider extends AbstractAuditTypeProvider implements AuditTypeProvider
{
    public static final String NARRATIVE_AUDIT_EVENT = "NarrativeAuditEvent";
    public static final String COLUMN_NAME_NARRATIVE = "Narrative";

    static final List<FieldKey> defaultVisibleColumns = new ArrayList<>();

    static {

        defaultVisibleColumns.add(FieldKey.fromParts(COLUMN_NAME_CREATED));
        defaultVisibleColumns.add(FieldKey.fromParts(COLUMN_NAME_CREATED_BY));
        defaultVisibleColumns.add(FieldKey.fromParts(COLUMN_NAME_IMPERSONATED_BY));
        defaultVisibleColumns.add(FieldKey.fromParts(COLUMN_NAME_COMMENT));
        defaultVisibleColumns.add(FieldKey.fromParts(COLUMN_NAME_NARRATIVE));
    }

    @Override
    protected AbstractAuditDomainKind getDomainKind()
    {
        return new org.labkey.snd.NarrativeAuditProvider.NarrativeAuditDomainKind();
    }

    @Override
    public String getEventName()
    {
        return NARRATIVE_AUDIT_EVENT;
    }

    @Override
    public String getLabel()
    {
        return "SND Narrative Events";
    }

    @Override
    public String getDescription()
    {
        return "Information about SND events that update narratives.";
    }

    @Override
    public List<FieldKey> getDefaultVisibleColumns()
    {
        return defaultVisibleColumns;
    }

    @Override
    public <K extends AuditTypeEvent> Class<K> getEventClass()
    {
        return (Class<K>)NarrativeAuditTypeEvent.class;
    }

    public static void addAuditEntry(Container container, User user, String narrative, String comment)
    {
        NarrativeAuditProvider.NarrativeAuditTypeEvent event = new NarrativeAuditProvider.NarrativeAuditTypeEvent(container.getId(), comment);
        event.setNarrative(narrative);

        AuditLogService.get().addEvent(user, event);
    }

    public static class NarrativeAuditTypeEvent extends AuditTypeEvent
    {
        private String _narrative;

        public NarrativeAuditTypeEvent()
        {
            super();
        }

        public NarrativeAuditTypeEvent(String container, String comment)
        {
            super(NARRATIVE_AUDIT_EVENT, container, comment);
        }

        public String getNarrative()
        {
            return _narrative;
        }

        public void setNarrative(String narrative)
        {
            _narrative = narrative;
        }
    }

    public static class NarrativeAuditDomainKind extends AbstractAuditDomainKind
    {
        public static final String NAME = "SNDNarrativeAuditDomain";
        public static String NAMESPACE_PREFIX = "Audit-" + NAME;

        private final Set<PropertyDescriptor> _fields;

        public NarrativeAuditDomainKind()
        {
            super(NARRATIVE_AUDIT_EVENT);

            Set<PropertyDescriptor> fields = new LinkedHashSet<>();
            fields.add(createPropertyDescriptor(COLUMN_NAME_NARRATIVE, PropertyType.STRING));
            _fields = Collections.unmodifiableSet(fields);
        }

        @Override
        public Set<PropertyDescriptor> getProperties()
        {
            return _fields;
        }

        @Override
        protected String getNamespacePrefix()
        {
            return NAMESPACE_PREFIX;
        }

        @Override
        public String getKindName()
        {
            return NAME;
        }
    }
}
