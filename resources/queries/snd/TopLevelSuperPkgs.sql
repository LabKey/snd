SELECT SuperPkgId,
PkgId,
PkgId.Description AS Description,
PkgId.Narrative AS Narrative,
IsPrimitive
FROM SuperPkgs
WHERE ParentSuperPkgId IS NULL