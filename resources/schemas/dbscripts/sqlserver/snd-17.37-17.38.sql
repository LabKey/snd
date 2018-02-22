
ALTER TABLE snd.EventData ADD ParentEventDataId INT;
ALTER TABLE snd.EventData DROP COLUMN Modified;
ALTER TABLE snd.EventData DROP COLUMN ModifiedBy;
ALTER TABLE snd.EventData DROP COLUMN Created;
ALTER TABLE snd.EventData DROP COLUMN CreatedBy;
