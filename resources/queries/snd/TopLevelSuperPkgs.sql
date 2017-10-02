SELECT SuperPkgId,
PkgId,
PkgId.Description AS Description,
PkgId.Narrative AS Narrative,
PkgId.Repeatable AS Repeatable,
IsPrimitive
FROM SuperPkgs
WHERE ParentSuperPkgId IS NULL