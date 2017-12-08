
EXEC core.fn_dropifexists 'Projects', 'snd', 'CONSTRAINT', 'IDX_SND_PROJECTS_CONTAINER';
EXEC core.fn_dropifexists 'Projects', 'snd', 'CONSTRAINT', 'IDX_SND_PROJECTS_OBJECTID';
EXEC core.fn_dropifexists 'ProjectItems', 'snd', 'CONSTRAINT', 'FK_SND_PROJECTITEMS_PARENTOBJECTID';
EXEC core.fn_dropifexists 'Events', 'snd', 'CONSTRAINT', 'FK_SND_EVENTS_PARENTOBJECTID';
GO

EXEC core.fn_dropifexists 'Projects', 'snd', 'TABLE', NULL
GO

CREATE TABLE snd.Projects (
   ProjectId            INTEGER              NOT NULL,
   RevisionNum          INTEGER              NOT NULL,
   ReferenceId          INTEGER              NOT NULL,
   StartDate            date                 NOT NULL,
   EndDate              date,
   Description          NVARCHAR(4000)       NOT NULL,
   Active				        BIT					         NOT NULL,
   ObjectId             UNIQUEIDENTIFIER     NOT NULL DEFAULT newid(),
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PROJECTS PRIMARY KEY NONCLUSTERED (ObjectId),
   CONSTRAINT FK_SND_PROJECTS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId)

)
GO

CREATE INDEX IDX_SND_PROJECTS_CONTAINER ON snd.Projects(Container);
CREATE UNIQUE CLUSTERED INDEX IDX_SND_PROJECTS_PROJECTID_REVNUM ON snd.Projects(ProjectId, RevisionNum);
GO
