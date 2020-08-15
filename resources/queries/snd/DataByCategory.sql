SELECT
ad.ObjectId,
ad.PropertyId,
ad.TypeTag,
ad.FloatValue,
ad.DateTimeValue,
ad.StringValue,
ad.MvIndicator,
ad.ObjectURI,
ad.EventDataAndName,
ad.EventData,
ad.EventData.SuperPkgId.PkgId,
ad.EventData.EventId,
ad.Container,
pc.CategoryId as CategoryId,
pc.Description as Category,
ad.StudyLSID as LSID
FROM AttributeData ad
JOIN PkgCategoryJunction pcj ON ad.EventData.SuperPkgId.PkgId = pcj.PkgId
JOIN PkgCategories pc ON pc.CategoryId = pcj.CategoryId