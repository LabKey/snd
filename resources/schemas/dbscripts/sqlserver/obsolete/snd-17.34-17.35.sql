
/* Recreate EventData table */
EXEC core.fn_dropifexists 'CodedEvents','snd','TABLE';
GO

/*==============================================================*/
/* Table: EventData                                             */
/*==============================================================*/
CREATE TABLE snd.EventData (
   EventDataId				  INTEGER         NOT NULL,
   EventId					    INTEGER         NOT NULL,
   SuperPkgId				    INTEGER         NOT NULL,
   ObjectURI				    LsidType		    NOT NULL,
   Container			      ENTITYID		    NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid						      LSIDType,

   CONSTRAINT PK_SND_EVENTDATA PRIMARY KEY (EventDataId),
   CONSTRAINT FK_SND_EVENTDATA_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_EVENTDATA_EVENTID FOREIGN KEY (EventId) REFERENCES snd.Events (EventId),
   CONSTRAINT FK_SND_EVENTDATA_SUPERPKGID FOREIGN KEY (SuperPkgId) REFERENCES snd.SuperPkgs(SuperPkgId)
)
GO

/* Recreate EventNoteId not as identity column for etls */
ALTER TABLE snd.EventNotes DROP PK_SND_EVENTNOTES;
ALTER TABLE snd.EventNotes DROP CONSTRAINT FK_SND_EVENTNOTES_EVENTNOTEID;
DROP INDEX IDX_SND_EVENTNOTES_EVENTNOTEID ON snd.EventNotes;
GO

ALTER TABLE snd.EventNotes DROP COLUMN EventNoteId;
GO

ALTER TABLE snd.EventNotes ADD EventNoteId INTEGER NOT NULL;
GO

ALTER TABLE snd.EventNotes ADD CONSTRAINT PK_SND_EVENTNOTES PRIMARY KEY (EventNoteId);
ALTER TABLE snd.EventNotes ADD CONSTRAINT FK_SND_EVENTNOTES_EVENTNOTEID FOREIGN KEY (EventNoteId) REFERENCES snd.Events (EventId)
CREATE INDEX IDX_SND_EVENTNOTES_EVENTNOTEID ON snd.EventNotes(EventNoteId);