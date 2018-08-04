
EXEC sp_rename 'snd.Events.Id', 'ParticipantId', 'COLUMN';

EXEC core.fn_dropifexists 'EventNotes', 'snd', 'CONSTRAINT', 'FK_SND_EVENTNOTES_EVENTNOTEID';
GO

ALTER TABLE snd.EventNotes ADD CONSTRAINT FK_SND_EVENTNOTES_EVENTID FOREIGN KEY (EventId) REFERENCES snd.Events (EventId)