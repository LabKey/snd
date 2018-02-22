package org.labkey.api.snd;

import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;
import org.labkey.api.gwt.client.model.GWTPropertyDescriptor;
import org.labkey.api.security.User;

import java.util.Map;

public class AttributeData
{
    private int _propertyId;
    private GWTPropertyDescriptor _propertyDescriptor;
    private String _value;

    public static final String ATTRIBUTE_DATA_PROPERTY_ID = "propertyId";
    public static final String ATTRIBUTE_DATA_PROPERTY_DESCRIPTOR = "propertyDescriptor";
    public static final String ATTRIBUTE_DATA_VALUE = "value";

    public AttributeData(int propertyId, GWTPropertyDescriptor propertyDescriptor, String value)
    {
        _propertyId = propertyId;
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

    public GWTPropertyDescriptor getPropertyDescriptor()
    {
        return _propertyDescriptor;
    }

    public void setPropertyDescriptor(GWTPropertyDescriptor propertyDescriptor)
    {
        _propertyDescriptor = propertyDescriptor;
    }

    public String getValue()
    {
        return _value;
    }

    public void setValue(String value)
    {
        _value = value;
    }

    public Map<String, Object> getAttributeDataRow()
    {
        Map<String, Object> attributeDataValues = new ArrayListMap<>();
        attributeDataValues.put(ATTRIBUTE_DATA_PROPERTY_ID, getPropertyId());
        attributeDataValues.put(ATTRIBUTE_DATA_VALUE, getValue());

        return attributeDataValues;
    }

    public JSONObject toJSON(Container c, User u)
    {
        JSONObject json = new JSONObject();
        json.put(ATTRIBUTE_DATA_PROPERTY_ID, getPropertyId());
        json.put(ATTRIBUTE_DATA_PROPERTY_DESCRIPTOR, SNDService.get().convertPropertyDescriptorToJson(c, u, getPropertyDescriptor(), true));
        json.put(ATTRIBUTE_DATA_VALUE, getValue());

        return json;
    }
}
