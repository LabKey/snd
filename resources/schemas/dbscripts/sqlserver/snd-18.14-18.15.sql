ALTER TABLE snd.EventsCache ADD Container entityid;
ALTER TABLE snd.EventsCache ADD CONSTRAINT FK_SND_EVENTSCACHE_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId);
CREATE INDEX IDX_SND_EVENTSCACHE_CONTAINER ON snd.EventsCache(Container);
GO