package org.labkey.api.snd;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.exp.ChangePropertyDescriptorException;
import org.labkey.api.exp.Handler;
import org.labkey.api.exp.Lsid;
import org.labkey.api.exp.TemplateInfo;
import org.labkey.api.exp.property.Domain;
import org.labkey.api.exp.property.DomainProperty;
import org.labkey.api.exp.property.DomainUtil;
import org.labkey.api.exp.property.PropertyService;
import org.labkey.api.gwt.client.model.GWTDomain;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.query.SimpleTableDomainKind;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.view.ActionURL;
import org.labkey.api.writer.ContainerUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by marty on 7/11/2017.
 */
public class SNDDomainKind extends SimpleTableDomainKind
{
    private final String NAMESPACE_PREFIX = "snd";
    private final String SCHEMA_NAME = "snd";
    private final String KIND_NAME = "SND";


    @Override
    public boolean canCreateDefinition(User user, Container container)
    {
        return container.hasPermission("SNDDomainKind.canCreateDefinition", user, AdminPermission.class);
    }

    @Override
    public Domain createDomain(GWTDomain gwtDomain, Map<String, Object> arguments, Container container, User user, TemplateInfo templateInfo)
    {
        if (gwtDomain.getName() == null)
            throw new IllegalArgumentException("table name is required");

        String domainURI = generateDomainURI(SCHEMA_NAME, gwtDomain.getName(), container, user);
        Domain domain = PropertyService.get().getDomain(container, domainURI);

        if( null != domain )
        {
            GWTDomain existingDomain = DomainUtil.getDomainDescriptor(user, domainURI, container);
            GWTDomain updatedDomain = new GWTDomain(existingDomain);
            updatedDomain.setFields(gwtDomain.getFields());

            updateDomain(existingDomain, updatedDomain, container, user);
        }
        else
        {
            List<GWTPropertyDescriptor> properties = gwtDomain.getFields();
            domain = PropertyService.get().createDomain(container, domainURI, gwtDomain.getName(), templateInfo);

            Set<String> propertyUris = new HashSet<>();
            Map<DomainProperty, Object> defaultValues = new HashMap<>();
            try
            {
                for (GWTPropertyDescriptor pd : properties)
                {
                    DomainUtil.addProperty(domain, pd, defaultValues, propertyUris, null);
                }
                domain.save(user);
            }
            catch (ChangePropertyDescriptorException e)
            {
                throw new RuntimeException(e);
            }
        }
        return domain;
    }

    @Override
    public String getKindName()
    {
        return KIND_NAME;
    }

    @Override
    public String getTypeLabel(Domain domain)
    {
        return domain.getName();
    }

    @Override
    public SQLFragment sqlObjectIdsInDomain(Domain domain)
    {
        return null;
    }

    @Override
    public ActionURL urlShowData(Domain domain, ContainerUser containerUser)
    {
        return null;
    }

    @Override
    public
    @Nullable ActionURL urlEditDefinition(Domain domain, ContainerUser containerUser)
    {
        return null;
    }

    @Override
    public Set<String> getReservedPropertyNames(Domain domain)
    {
        Set<String> result = new HashSet<>();
        result.add("Description");
        result.add("Active");
        result.add("ObjectId");
        result.add("QcState");
        result.add("Container");
        result.add("CreatedBy");
        result.add("Created");
        result.add("ModifiedBy");
        result.add("Modified");
        return result;
    }

    @Override
    public Handler.Priority getPriority(String domainURI)
    {
        Lsid lsid = new Lsid(domainURI);
        return lsid.getNamespacePrefix() != null && lsid.getNamespacePrefix().startsWith(NAMESPACE_PREFIX) ? Handler.Priority.MEDIUM : null;
    }
}
