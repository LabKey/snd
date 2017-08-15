package org.labkey.api.snd;

import java.util.List;

/**
 * Created by marty on 8/14/2017.
 */
public class SuperPackage
{
    private Integer _superPkgId;
    private List<Integer> _childPackages;
    private Integer _pkgId;

    public Integer getSuperPkgId()
    {
        return _superPkgId;
    }

    public void setSuperPkgId(Integer superPkgId)
    {
        _superPkgId = superPkgId;
    }

    public List<Integer> getParentSuperPkgId()
    {
        return _childPackages;
    }

    public void setParentSuperPkgId(List<Integer> childPackages)
    {
        _childPackages = childPackages;
    }

    public Integer getPkgId()
    {
        return _pkgId;
    }

    public void setPkgId(Integer pkgId)
    {
        _pkgId = pkgId;
    }
}
