/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.gwt.client.model.GWTPropertyValidator;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    private boolean _hasEvent;
    private boolean _hasProject;
    private List<Integer> _categories = new ArrayList<>();
    private List<GWTPropertyDescriptor> _attributes = new ArrayList<>();
    private List<SuperPackage> _subpackages;
    private Map<GWTPropertyDescriptor, Object> _extraFields = new HashMap<>();
    private Map<String, String> _lookups = new HashMap<>();
    private Integer _qcState;

    public static final String PKG_ID = "pkgId";
    public static final String PKG_DESCRIPTION = "description";
    public static final String PKG_ACTIVE = "active";
    public static final String PKG_REPEATABLE = "repeatable";
    public static final String PKG_QCSTATE = "qcState";
    public static final String PKG_NARRATIVE = "narrative";
    public static final String PKG_CONTAINER = "container";
    public static final String PKG_HASEVENT = "hasEvent";
    public static final String PKG_HASPROJECT = "hasProject";

    public static final String PKG_CATEGORIES = "categories";
    public static final String PKG_ATTRIBUTES = "attributes";
    public static final String PKG_SUBPACKAGES = "subPackages";

    public static final String RANGE_PARTICIPANTID = "ParticipantId";

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

    public boolean hasEvent()
    {
        return _hasEvent;
    }

    public void setHasEvent(boolean hasEvent)
    {
        _hasEvent = hasEvent;
    }

    public boolean hasProject()
    {
        return _hasProject;
    }

    public void setHasProject(boolean hasProject)
    {
        _hasProject = hasProject;
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

    public List<SuperPackage> getSubpackages()
    {
        return _subpackages;
    }

    public void setSubpackages(List<SuperPackage> subpackages)
    {
        this._subpackages = subpackages;
    }

    public Map<GWTPropertyDescriptor, Object> getExtraFields()
    {
        return _extraFields;
    }

    public void setExtraFields(Map<GWTPropertyDescriptor, Object> extraFields)
    {
        this._extraFields = extraFields;
    }

    public Map<String, String> getLookups()
    {
        return _lookups;
    }

    public void setLookups(Map<String, String> lookups)
    {
        _lookups = lookups;
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
        pkgValues.put(PKG_ID, getPkgId());
        pkgValues.put(PKG_NARRATIVE, getNarrative());
        pkgValues.put(PKG_DESCRIPTION, getDescription());
        pkgValues.put(PKG_ACTIVE, isActive());
        pkgValues.put(PKG_REPEATABLE, isRepeatable());
        pkgValues.put(PKG_QCSTATE, getQcState());
        pkgValues.put(PKG_CONTAINER, c);

        Map<GWTPropertyDescriptor, Object> extras = getExtraFields();
        for (GWTPropertyDescriptor gpd : extras.keySet())
        {
            pkgValues.put(gpd.getName(), extras.get(gpd));
        }

        return pkgValues;
    }

    public List<Map<String, Object>> getCategoryRows(Container c)
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row;

        for (Integer categoryId : getCategories())
        {
            row = new ArrayListMap<>();
            row.put(PKG_ID, getPkgId());
            row.put("categoryId", categoryId);
            row.put(PKG_CONTAINER, c);
            rows.add(row);
        }

        return rows;
    }

    public JSONArray lookupValuesToJson(Container c, User u, String schema, String query)
    {
        JSONArray array = new JSONArray();

        UserSchema userSchema = QueryService.get().getUserSchema(u, c, schema);
        TableInfo table = userSchema.getTable(query);

        if (null != table)
        {
            List<String> pks = table.getPkColumnNames();
            TableSelector ts = new TableSelector(table);
            Object value;
            try(ResultSet rs = ts.getResultSet())
            {
                while (rs.next())
                {
                    value = rs.getObject(pks.get(0));
                    array.put(value);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        return array;
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

    public JSONObject convertPropertyDescriptorToJson(Container c, User u, GWTPropertyDescriptor pd, boolean resolveLookupValues)
    {
        JSONObject json = new JSONObject();
        json.put("name", pd.getName());
        json.put("required", pd.isRequired());
        json.put("label", pd.getLabel());
        json.put("scale", pd.getScale());
        json.put("format", pd.getFormat());
        json.put("lookupSchema", pd.getLookupSchema());
        json.put("lookupQuery", pd.getLookupQuery());
        if (pd.getLookupSchema() != null && pd.getLookupQuery() != null && pd.getDefaultValue() != null)
        {
            json.put("defaultValue", SNDService.get().getDefaultLookupDisplayValue(u, c, pd.getLookupSchema(), pd.getLookupQuery(), pd.getDefaultValue()));
        }
        else
        {
            json.put("defaultValue", pd.getDefaultValue());
        }
        json.put("redactedText", pd.getRedactedText());
        json.put("validators", convertPropertyValidatorsToJson(pd));
        if (resolveLookupValues && (pd.getLookupSchema() != null) && (pd.getLookupQuery() != null))
        {
            json.put("lookupValues", lookupValuesToJson(c, u, pd.getLookupSchema(), pd.getLookupQuery()));
        }

        // Not passing in full range URI also need to handle participantid
        String type = pd.getRangeURI().split("#")[1];
        String conceptUri = pd.getConceptURI();
        if (conceptUri != null && conceptUri.contains(RANGE_PARTICIPANTID))
        {
            type = RANGE_PARTICIPANTID;
        }
        json.put("rangeURI", type);

        return json;
    }

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(PKG_ID, getPkgId());
        json.put(PKG_DESCRIPTION, getDescription());
        json.put(PKG_REPEATABLE, isRepeatable());
        json.put(PKG_ACTIVE, isActive());
        json.put(PKG_NARRATIVE, getNarrative());
        json.put(PKG_HASEVENT, hasEvent());
        json.put(PKG_HASPROJECT, hasProject());
        json.put(PKG_QCSTATE, getQcState());
        json.put(PKG_CONTAINER, c.getId());

        JSONArray categories = new JSONArray();
        if(getCategories() != null)
        {
            for (Integer categoryId : getCategories())
            {
                categories.put(categoryId);
            }
            json.put(PKG_CATEGORIES, categories);
        }

        JSONArray attributes = new JSONArray();
        if(getAttributes() != null)
        {
            for (GWTPropertyDescriptor pd : getAttributes())
            {
                attributes.put(convertPropertyDescriptorToJson(c, u, pd, false));
            }
            json.put(PKG_ATTRIBUTES, attributes);
        }

        JSONArray subPackages = new JSONArray();
        if(getSubpackages() != null)
        {
            for (SuperPackage subPackage : getSubpackages())
            {
                subPackages.put(subPackage.toJson());
            }
            json.put(PKG_SUBPACKAGES, subPackages);
        }

        JSONArray extras = new JSONArray();
        Map<GWTPropertyDescriptor, Object> extraFields = getExtraFields();
        if(extraFields != null)
        {
            JSONObject jsonExtra;
            for (GWTPropertyDescriptor extraPd : extraFields.keySet())
            {
                jsonExtra = convertPropertyDescriptorToJson(c, u, extraPd, true);
                jsonExtra.put("value", extraFields.get(extraPd));
                extras.put(jsonExtra);
            }

            json.put("extraFields", extras);
        }

        JSONArray lookups = new JSONArray();
        Map<String, String> lookupMap = getLookups();

        JSONObject jsonLookup;
        for (String key : lookupMap.keySet())
        {
            jsonLookup = new JSONObject();
            jsonLookup.put("value", key);
            jsonLookup.put("label", lookupMap.get(key));
            lookups.put(jsonLookup);
        }

        json.put("attributeLookups", lookups);

        return json;
    }
}
