package org.labkey.api.snd;

import org.json.JSONArray;
import org.json.JSONObject;
import org.labkey.api.collections.ArrayListMap;
import org.labkey.api.data.Container;

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
    private String _description;

    public static final String SUPERPKG_ID = "superPkgId";
    public static final String SUPERPKG_PARENTID = "parentSuperPkgId";
    public static final String SUPERPKG_PKGID = "pkgId";
    public static final String SUPERPKG_DESCRIPTION = "description";  // From referenced package

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

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
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
        superPkgValues.put("SuperPkgId", getSuperPkgId());
        superPkgValues.put("ParentSuperPkgId", getParentSuperPkgId());
        superPkgValues.put("PkgId", getPkgId());
        superPkgValues.put("Container", c);
        superPkgValues.put("SuperPkgPath", getSuperPkgPath());

        return superPkgValues;
    }

    public JSONObject toJson()
    {
        JSONObject json = new JSONObject();
        json.put(SUPERPKG_ID, getSuperPkgId());
        json.put(SUPERPKG_PKGID, getPkgId());
        json.put(SUPERPKG_DESCRIPTION, getDescription());

        JSONArray subPackages = new JSONArray();
        if (getChildPackages() != null)
        {
            for (SuperPackage subPackage : getChildPackages())
            {
                subPackages.put(subPackage.toJson());
            }
        }

        json.put("subPackages", subPackages);
        return json;
    }
}
