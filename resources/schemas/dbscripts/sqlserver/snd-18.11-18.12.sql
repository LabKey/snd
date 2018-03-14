/*==============================================================*/
/* Table: EventsCache                                           */
/*==============================================================*/
CREATE TABLE snd.EventsCache (
  EventId              INTEGER              NOT NULL,
  HtmlNarrative        NVARCHAR(MAX),

  CONSTRAINT PK_SND_EVENTS_CACHE PRIMARY KEY (EventId)
)
  GO