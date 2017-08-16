package org.labkey.api.snd;

import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marty on 8/4/2017.
 */
public class Package
{
    private Integer _pkgId;
    private String _description;
    private String _narrative;
    private boolean _repeatable;
    private boolean _active;
    private List<Integer> _categories = new ArrayList<>();
    private List<GWTPropertyDescriptor> _attributes;
    private List<Integer> _subpackages;
    private Map<String, Object> _extraFields = new HashMap<>();

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

    public String getNarrative()
    {
        return _narrative;
    }

    public void setNarrative(String narrative)
    {
        _narrative = narrative;
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

    public List<Integer> getCategories()
    {
        return _categories;
    }

    public void setCategories(List<Integer> categories)
    {
        this._categories = categories;
    }

    public List<GWTPropertyDescriptor> getAttributes()
    {
        return _attributes;
    }

    public void setAttributes(List<GWTPropertyDescriptor> attributes)
    {
        this._attributes = attributes;
    }

    public Collection<Integer> getSubpackages()
    {
        return _subpackages;
    }

    public void setSubpackages(List<Integer> subpackages)
    {
        this._subpackages = subpackages;
    }

    public Map<String, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields)
    {
        this._extraFields = extraFields;
    }

    public Map<String, Object> getPackageRow(Container c)
    {
        Map<String, Object> pkgValues = new ArrayListMap<>();
        pkgValues.put("PkgId", getPkgId());
        pkgValues.put("Narrative", getNarrative());
        pkgValues.put("Description", getDescription());
        pkgValues.put("Active", isActive());
        pkgValues.put("Repeatable", isRepeatable());
        pkgValues.put("Container", c);
        pkgValues.putAll(getExtraFields());

        return pkgValues;
    }

    public List<Map<String, Object>> getCategoryRows(Container c)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row;

        for (Integer categoryId : getCategories())
        {
            row = new ArrayListMap<>();
            row.put("PkgId", getPkgId());
            row.put("CategoryId", categoryId);
            row.put("Container", c);
            rows.add(row);
        }

        return rows;
    }
}
