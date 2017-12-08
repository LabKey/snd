EXEC core.fn_dropifexists 'ProjectItems', 'snd', 'CONSTRAINT', 'FK_SND_PROJECTITEMS_PARENTOBJECTID';
EXEC core.fn_dropifexists 'Events', 'snd', 'CONSTRAINT', 'FK_SND_EVENTS_PARENTOBJECTID';
GO

DELETE FROM snd.ProjectItems;
DELETE FROM snd.CodedEvents;
DELETE FROM snd.Events;
GO

ALTER TABLE snd.ProjectItems
ADD CONSTRAINT FK_SND_PROJECTITEMS_PARENTOBJECTID FOREIGN KEY (ParentObjectId) REFERENCES snd.Projects (ObjectId)

ALTER TABLE snd.Events
ADD CONSTRAINT FK_SND_EVENTS_PARENTOBJECTID FOREIGN KEY (ParentObjectId) REFERENCES snd.Projects (ObjectId)