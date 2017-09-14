package org.labkey.api.snd;

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
}
