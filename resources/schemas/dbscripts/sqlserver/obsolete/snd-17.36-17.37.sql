ALTER TABLE snd.Lookups ADD   [ObjectId] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID();
CREATE UNIQUE INDEX idx_snd_lookups_objectid ON snd.lookups (ObjectId);

ALTER TABLE snd.LookupSets ADD [ObjectId] UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID();
CREATE UNIQUE INDEX idx_snd_lookupSets_objectid ON snd.lookupSets (ObjectId);

