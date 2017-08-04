package org.labkey.api.snd;

import org.labkey.api.exp.PropertyDescriptor;

import java.util.Collection;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDPackage
{
    private int pkgId;
    private String description;
    private boolean repeatable;
    private boolean draft;
    private Collection<Integer> categories;
    private Collection<PropertyDescriptor> attributes;
    private Collection<Integer> subpackages;
    private Collection<PropertyDescriptor> extraFields;

    public int getPkgId()
    {
        return pkgId;
    }

    public void setPkgId(int pkgId)
    {
        this.pkgId = pkgId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isRepeatable()
    {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable)
    {
        this.repeatable = repeatable;
    }

    public boolean isDraft()
    {
        return draft;
    }

    public void setDraft(boolean draft)
    {
        this.draft = draft;
    }

    public Collection<Integer> getCategories()
    {
        return categories;
    }

    public void setCategories(Collection<Integer> categories)
    {
        this.categories = categories;
    }

    public Collection<PropertyDescriptor> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Collection<PropertyDescriptor> attributes)
    {
        this.attributes = attributes;
    }

    public Collection<Integer> getSubpackages()
    {
        return subpackages;
    }

    public void setSubpackages(Collection<Integer> subpackages)
    {
        this.subpackages = subpackages;
    }

    public Collection<PropertyDescriptor> getExtraFields()
    {
        return extraFields;
    }

    public void setExtraFields(Collection<PropertyDescriptor> extraFields)
    {
        this.extraFields = extraFields;
    }
}
