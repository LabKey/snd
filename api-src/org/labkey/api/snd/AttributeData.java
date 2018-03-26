package org.labkey.api.snd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;

import java.util.Map;

/**
 * Class for attribute data and related methods. Used in EventData class
 */
public class AttributeData
{
    private int _propertyId = -1;
    private String _propertyName;
    private GWTPropertyDescriptor _propertyDescriptor;
    private String _value;

    public static final String ATTRIBUTE_DATA_PROPERTY_ID = "propertyId";
    public static final String ATTRIBUTE_DATA_PROPERTY_NAME = "propertyName";
    public static final String ATTRIBUTE_DATA_PROPERTY_DESCRIPTOR = "propertyDescriptor";
    public static final String ATTRIBUTE_DATA_VALUE = "value";

    public static final String ATTRIBUTE_DATA_CSS_CLASS = "snd-attribute-data";

    public AttributeData(int propertyId, @Nullable GWTPropertyDescriptor propertyDescriptor, @Nullable String value)
    {
        _propertyId = propertyId;
        _propertyDescriptor = propertyDescriptor;
        _value = value;
    }

    public AttributeData(String propertyName, @Nullable GWTPropertyDescriptor propertyDescriptor, @Nullable String value)
    {
        _propertyName = propertyName;
        _propertyDescriptor = propertyDescriptor;
        _value = value;
    }

    public AttributeData()
    {}

    public int getPropertyId()
    {
        return _propertyId;
    }

    public void setPropertyId(int propertyId)
    {
        _propertyId = propertyId;
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyName = propertyName;
    }

    @Nullable
    public GWTPropertyDescriptor getPropertyDescriptor()
    {
        return _propertyDescriptor;
    }

    public void setPropertyDescriptor(GWTPropertyDescriptor propertyDescriptor)
    {
        _propertyDescriptor = propertyDescriptor;
    }

    @Nullable
    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }

    @NotNull
    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_DATA_PROPERTY_ID, getPropertyId());
        json.put(ATTRIBUTE_DATA_PROPERTY_NAME, getPropertyName());
        json.put(ATTRIBUTE_DATA_PROPERTY_DESCRIPTOR, SNDService.get().convertPropertyDescriptorToJson(c, u, getPropertyDescriptor(), true));
        json.put(ATTRIBUTE_DATA_VALUE, getValue());

        return json;
    }
}
