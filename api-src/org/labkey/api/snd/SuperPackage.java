package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by marty on 8/14/2017.
 */
public class SuperPackage
{
    private Integer _superPkgId;
    private List<SuperPackage> _childPackages;
    private Integer _pkgId;
    private String _superPkgPath;
    private Integer _parentSuperPkgId;
    private String _description; // From referenced package
    private String _narrative; // From referenced package
    private Integer _sortOrder;
    private Boolean _repeatable;
    // NOTE: if you add a variable here, add it to the copy constructor, getSuperPackageRow(), and toJson() too!

    public static final String SUPERPKG_ID = "SuperPkgId";
    public static final String SUPERPKG_PARENTID = "ParentSuperPkgId";
    public static final String SUPERPKG_PKGID = "PkgId";
    public static final String SUPERPKG_DESCRIPTION = "Description";
    public static final String SUPERPKG_NARRATIVE = "Narrative";
    public static final String SUPERPKG_ORDER = "SortOrder";
    public static final String SUPERPKG_REPEATABLE = "Repeatable";
    public static final String SUPERPKG_PATH = "SuperPkgPath";

    public SuperPackage()
    {

    }

    // copy constructor
    public SuperPackage(SuperPackage _superPackage)
    {
        this._superPkgId = _superPackage.getSuperPkgId();  // Integers are immutable
        this._childPackages = new ArrayList<>();
        List<SuperPackage> childPackages = _superPackage.getChildPackages();
        if (childPackages != null)
        {
            for (SuperPackage childPackage : _superPackage.getChildPackages())
                this._childPackages.add(new SuperPackage(childPackage));
        }
        this._pkgId = _superPackage.getPkgId();
        this._superPkgId = _superPackage.getSuperPkgId();  // Strings are also immutable
        this._parentSuperPkgId = _superPackage.getParentSuperPkgId();
        this._description = _superPackage.getDescription();
        this._narrative = _superPackage.getNarrative();
        this._sortOrder = _superPackage.getSortOrder();
        this._repeatable = _superPackage.getRepeatable();
    }

    public Integer getParentSuperPkgId()
    {
        return _parentSuperPkgId;
    }

    public void setParentSuperPkgId(Integer parentSuperPkgId)
    {
        _parentSuperPkgId = parentSuperPkgId;
    }

    public List<SuperPackage> getChildPackages()
    {
        return _childPackages;
    }

    public Integer getSortOrder()
    {
        return _sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        _sortOrder = sortOrder;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public Boolean getRepeatable() { return _repeatable; }

    public void setRepeatable(Boolean repeatable) { _repeatable = repeatable; }

    public String getNarrative()
    {
        return _narrative;
    }

    public void setNarrative(String narrative)
    {
        _narrative = narrative;
    }

    public void setChildPackages(List<SuperPackage> childPackages)
    {
        _childPackages = childPackages;
    }

    public String getSuperPkgPath()
    {
        return _superPkgPath;
    }

    public void setSuperPkgPath(String superPkgPath)
    {
        _superPkgPath = superPkgPath;
    }

    public Integer getSuperPkgId()
    {
        return _superPkgId;
    }

    public void setSuperPkgId(Integer superPkgId)
    {
        _superPkgId = superPkgId;
    }


    public Integer getPkgId()
    {
        return _pkgId;
    }

    public void setPkgId(Integer pkgId)
    {
        _pkgId = pkgId;
    }

    public Map<String, Object> getSuperPackageRow(Container c)
    {
        Map<String, Object> superPkgValues = new ArrayListMap<>();
        superPkgValues.put(SUPERPKG_ID, getSuperPkgId());
        superPkgValues.put(SUPERPKG_PARENTID, getParentSuperPkgId());
        superPkgValues.put(SUPERPKG_PKGID, getPkgId());
        superPkgValues.put(SUPERPKG_ORDER, getSortOrder());
        superPkgValues.put(SUPERPKG_REPEATABLE, getRepeatable());
        superPkgValues.put("Container", c);
        superPkgValues.put(SUPERPKG_PATH, getSuperPkgPath());

        return superPkgValues;
    }

    public JSONObject toJson()
    {
        JSONObject json = new JSONObject();
        json.put(SUPERPKG_ID, getSuperPkgId());
        json.put(SUPERPKG_PKGID, getPkgId());
        json.put(SUPERPKG_DESCRIPTION, getDescription());
        json.put(SUPERPKG_NARRATIVE, getNarrative());
        json.put(SUPERPKG_ORDER, getSortOrder());
        json.put(SUPERPKG_REPEATABLE, getRepeatable());

        JSONArray subPackages = new JSONArray();
        if (getChildPackages() != null)
        {
            for (SuperPackage subPackage : getChildPackages())
            {
                subPackages.put(subPackage.toJson());
            }
        }

        json.put("SubPackages", subPackages);
        return json;
    }
}
