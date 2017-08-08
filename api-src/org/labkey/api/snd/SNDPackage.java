package org.labkey.api.snd;

import org.labkey.api.exp.PropertyDescriptor;

import java.util.Collection;

/**
 * Created by marty on 8/4/2017.
 */
public class SNDPackage
{
    private Integer _pkgId;
    private String _description;
    private boolean _repeatable;
    private boolean _active;
    private Collection<Integer> _categories;
    private Collection<PropertyDescriptor> _attributes;
    private Collection<Integer> _subpackages;
    private Collection<PropertyDescriptor> _extraFields;

    public Integer getPkgId()
    {
        return _pkgId;
    }

    public void setPkgId(int pkgId)
    {
        this._pkgId = pkgId;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        this._description = description;
    }

    public boolean isRepeatable()
    {
        return _repeatable;
    }

    public void setRepeatable(boolean repeatable)
    {
        this._repeatable = repeatable;
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean active)
    {
        this._active = active;
    }

    public Collection<Integer> getCategories()
    {
        return _categories;
    }

    public void setCategories(Collection<Integer> categories)
    {
        this._categories = categories;
    }

    public Collection<PropertyDescriptor> getAttributes()
    {
        return _attributes;
    }

    public void setAttributes(Collection<PropertyDescriptor> attributes)
    {
        this._attributes = attributes;
    }

    public Collection<Integer> getSubpackages()
    {
        return _subpackages;
    }

    public void setSubpackages(Collection<Integer> subpackages)
    {
        this._subpackages = subpackages;
    }

    public Collection<PropertyDescriptor> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Collection<PropertyDescriptor> extraFields)
    {
        this._extraFields = extraFields;
    }
}
