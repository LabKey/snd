DROP TABLE snd.Lookups
GO

DROP TABLE snd.LookupSets
GO

/*==============================================================*/
/* Table: LookupSets                                            */
/*==============================================================*/
CREATE TABLE snd.LookupSets (
   LookupSetId          INTEGER IDENTITY(1,1)     NOT NULL,
   SetName              NVARCHAR(128)             NOT NULL,
   Label                NVARCHAR(128),
   Description          NVARCHAR(900),
   Container			      ENTITYID			            NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_LOOKUPSETS PRIMARY KEY (LookupSetId),
   CONSTRAINT FK_SND_LOOKUPSETS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId)
)
GO

CREATE INDEX IDX_SND_LOOKUPSETS_CONTAINER ON snd.LookupSets(Container);
GO

/*==============================================================*/
/* Table: Lookups                                               */
/*==============================================================*/
CREATE TABLE snd.Lookups (
   LookupSetId          INTEGER                   NOT NULL,
   Value                NVARCHAR(446)             NOT NULL,
   Displayable          BIT                       NOT NULL,
   SortOrder            INTEGER,
   Container			      ENTITYID			            NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_LOOKUPS PRIMARY KEY (LookupSetId, Value),
   CONSTRAINT FK_SND_LOOKUPS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_LOOKUPS_LOOKUPSETID FOREIGN KEY (LookupSetId) REFERENCES snd.LookupSets (LookupSetId)
)

CREATE INDEX IDX_SND_LOOKUPS_CONTAINER ON snd.Lookups(Container);
CREATE INDEX IDX_SND_LOOKUPS_LOOKUPSETID ON snd.Lookups(LookupSetId);
GO