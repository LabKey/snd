package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.gwt.client.model.GWTPropertyValidator;

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
    private List<GWTPropertyDescriptor> _attributes = new ArrayList<>();
    private List<Integer> _subpackages;
    private Map<String, Object> _extraFields = new HashMap<>();
    private Integer _qcState;

    public static final String PKGID_COL = "pkgId";
    public static final String DESCRIPTION_COL = "description";
    public static final String ACTIVE_COL = "active";
    public static final String REPEATABLE_COL = "repeatable";
    public static final String QCSTATE_COL = "qcState";
    public static final String NARRATIVE_COL = "narrative";
    public static final String CONTAINER_COL = "container";

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

    public Integer getQcState()
    {
        return _qcState;
    }

    public void setQcState(Integer qcState)
    {
        _qcState = qcState;
    }

    public Map<String, Object> getPackageRow(Container c)
    {
        Map<String, Object> pkgValues = new ArrayListMap<>();
        pkgValues.put(PKGID_COL, getPkgId());
        pkgValues.put(NARRATIVE_COL, getNarrative());
        pkgValues.put(DESCRIPTION_COL, getDescription());
        pkgValues.put(ACTIVE_COL, isActive());
        pkgValues.put(REPEATABLE_COL, isRepeatable());
        pkgValues.put(QCSTATE_COL, getQcState());
        pkgValues.put(CONTAINER_COL, c);
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
            row.put(PKGID_COL, getPkgId());
            row.put("categoryId", categoryId);
            row.put(CONTAINER_COL, c);
            rows.add(row);
        }

        return rows;
    }

    public JSONArray convertPropertyValidatorsToJson(GWTPropertyDescriptor pd)
    {
        JSONArray json = new JSONArray();
        JSONObject obj;
        for (GWTPropertyValidator pv : pd.getPropertyValidators())
        {
            obj = new JSONObject();
            obj.put("name", pv.getName());
            obj.put("description", pv.getDescription());
            obj.put("type", pv.getType().getTypeName());
            obj.put("expression", pv.getExpression());
            obj.put("errorMessage", pv.getErrorMessage());
            json.put(obj);
        }

        return json;
    }

    public JSONObject convertPropertyDescriptorToJson(GWTPropertyDescriptor pd)
    {
        JSONObject json = new JSONObject();
        json.put("name", pd.getName());
        json.put("rangeURI", pd.getRangeURI());
        json.put("required", pd.isRequired());
        json.put("label", pd.getLabel());
        json.put("scale", pd.getScale());
        json.put("format", pd.getFormat());
        json.put("lookupSchema", pd.getLookupSchema());
        json.put("lookupQuery", pd.getLookupQuery());
        json.put("validators", convertPropertyValidatorsToJson(pd));

        return json;
    }

    public JSONObject toJSON(Container c)
    {
        JSONObject json = new JSONObject();
        json.put(PKGID_COL, getPkgId());
        json.put(DESCRIPTION_COL, getDescription());
        json.put(REPEATABLE_COL, isRepeatable());
        json.put(ACTIVE_COL, isActive());
        json.put(NARRATIVE_COL, getNarrative());
        json.put(QCSTATE_COL, getQcState());
        json.put(CONTAINER_COL, c.getId());

        JSONArray categories = new JSONArray();
        for (Integer categoryId : getCategories())
        {
            categories.put(categoryId);
        }
        json.put("categories", categories);

        JSONArray attributes = new JSONArray();
        for (GWTPropertyDescriptor pd : getAttributes())
        {
            attributes.put(convertPropertyDescriptorToJson(pd));
        }
        json.put("attributes", attributes);

        return json;
    }
}
