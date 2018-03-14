ALTER TABLE snd.EventsCache
  ADD CONSTRAINT FK_EventsCache_EventId FOREIGN KEY (EventId) REFERENCES snd.Events (EventId);
GO