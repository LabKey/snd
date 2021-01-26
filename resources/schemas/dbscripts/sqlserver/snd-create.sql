CREATE VIEW snd.mv_dataByCategory
WITH SCHEMABINDING
AS

SELECT
    ev.SubjectId,
    ev.Date,
    ev.QcState,
    ev.EventId,
    ev.ParentObjectId,
    ed.EventDataId,
    pd.Name as AttributeName,
    op.PropertyId,
    op.DateTimeValue,
    op.FloatValue,
    op.StringValue,
    op.TypeTag,
    pc.CategoryId,
    pc.Description as CategoryName,
    o.Container,
    o.ObjectURI,
    o.ObjectId
FROM snd.EventData ed
    INNER JOIN exp.Object o
    ON ed.ObjectURI = o.ObjectURI
    INNER JOIN exp.ObjectProperty op
    ON o.ObjectId = op.ObjectId
    INNER JOIN exp.PropertyDescriptor pd
    ON op.PropertyId = pd.PropertyId
    INNER JOIN snd.SuperPkgs sp
    ON ed.SuperPkgId = sp.SuperPkgId
    INNER JOIN snd.PkgCategoryJunction pj
    ON sp.PkgId = pj.PkgId
    INNER JOIN snd.PkgCategories pc
    ON pj.CategoryId = pc.CategoryId
    INNER JOIN snd.Events ev
    ON ed.EventId = ev.EventId
GO

CREATE UNIQUE CLUSTERED INDEX IDX_SND_MV_DATA_BY_CATEGORY ON snd.mv_dataByCategory (EventDataId, PropertyId, CategoryId);