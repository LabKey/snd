/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* snd-17.20-17.21.sql */

-- Create schema, tables, indexes, and constraints used for SND module here
-- All SQL VIEW definitions should be created in snd-create.sql and dropped in snd-drop.sql

EXEC core.fn_dropifexists 'CodedEvents','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'EventNotes','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'EventsCache','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'PkgCategoryJunction','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'ProjectItems','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'SuperPkgs','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'PkgCategories','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'Pkgs','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'Events','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists 'Projects','codedprocs','TABLE';
GO

EXEC core.fn_dropifexists NULL,'codedprocs','SCHEMA';
GO

CREATE SCHEMA snd;
GO

/*==============================================================*/
/* Table: Pkgs                                                  */
/*==============================================================*/
CREATE TABLE snd.Pkgs (
   PkgId                INTEGER              NOT NULL,
   Description          NVARCHAR(4000)       NOT NULL,
   Active               BIT,
   Repeatable           BIT,
   QcState              INTEGER,
   ObjectId             UNIQUEIDENTIFIER     NOT NULL DEFAULT newid(),
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PKGS PRIMARY KEY (PkgId),
   CONSTRAINT FK_SND_PKGS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_PKGS_QCSTATE FOREIGN Key (QcState) REFERENCES core.QCState (RowId)

)
GO

CREATE INDEX IDX_SND_PKGS_CONTAINER ON snd.Pkgs(Container);
CREATE INDEX IDX_SND_PKGS_QCSTATE ON snd.Pkgs(QcState);
GO

/*==============================================================*/
/* Table: SuperPkgs                                             */
/*==============================================================*/
CREATE TABLE snd.SuperPkgs (
   SuperPkgId           INTEGER              NOT NULL,
   ParentSuperPkgId     INTEGER,
   PkgId                INTEGER              NOT NULL,
   SuperPkgPath         VARCHAR(900)         NOT NULL,
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_SUPERPKGS PRIMARY KEY (SuperPkgId),
   CONSTRAINT FK_SND_SUPERPKGS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_SUPERPKGS_PKGID FOREIGN KEY (PkgId) REFERENCES snd.Pkgs (PkgId)

)
GO

CREATE INDEX IDX_SND_SUPERPKGS_CONTAINER ON snd.SuperPkgs(Container);
CREATE INDEX IDX_SND_SUPERPKGS_PKGID ON snd.SuperPkgs(PkgId);
GO

/*==============================================================*/
/* Table: PkgCategories                                         */
/*==============================================================*/
CREATE TABLE snd.PkgCategories (
   CategoryId           INTEGER              NOT NULL,
   Description          NVARCHAR(4000)       NOT NULL,
   Comment              NVARCHAR(4000),
   Active               BIT                  NOT NULL,
   SortOrder            INTEGER,
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PKGCATEGORIES PRIMARY KEY (CategoryId),
   CONSTRAINT FK_SND_PKGCATEGORIES_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId)

)
GO

CREATE INDEX IDX_SND_PKGCATEGORIES_CONTAINER ON snd.PkgCategories(Container);
GO

/*==============================================================*/
/* Table: PkgCategoryJunction                                   */
/*==============================================================*/
CREATE TABLE snd.PkgCategoryJunction (
   PkgId                INTEGER              NOT NULL,
   CategoryId           INTEGER              NOT NULL,
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PKGCATEGORYJUNCTION PRIMARY KEY (PkgId, CategoryId),
   CONSTRAINT FK_SND_PKGCATEGORYJUNCTION_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_PKGCATEGORYJUNCTION_PKGID FOREIGN KEY (PkgId) REFERENCES snd.Pkgs (PkgId),
   CONSTRAINT FK_SND_PKGCATEGORYJUNCTION_CATEGORYID FOREIGN KEY (CategoryId) REFERENCES snd.PkgCategories (CategoryId)

)
GO

CREATE INDEX IDX_SND_PKGCATEGORYJUNCTION_CONTAINER ON snd.PkgCategoryJunction(Container);
CREATE INDEX IDX_SND_PKGCATEGORYJUNCTION_PKGID ON snd.PkgCategoryJunction(PkgId);
CREATE INDEX IDX_SND_PKGCATEGORYJUNCTION_CATEGORYID ON snd.PkgCategoryJunction(CategoryId);
GO

/*==============================================================*/
/* Table: Projects                                              */
/*==============================================================*/
CREATE TABLE snd.Projects (
   ProjectId            INTEGER              NOT NULL,
   RevisionNum          INTEGER              NOT NULL,
   ReferenceId          INTEGER              NOT NULL,
   StartDate            date                 NOT NULL,
   EndDate              date,
   Description          NVARCHAR(4000)       NOT NULL,
   ObjectId             UNIQUEIDENTIFIER     NOT NULL DEFAULT newid(),
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PROJECTS PRIMARY KEY (ProjectId, RevisionNum),
   CONSTRAINT FK_SND_PROJECTS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId)

)
GO

CREATE INDEX IDX_SND_PROJECTS_CONTAINER ON snd.Projects(Container);
CREATE UNIQUE INDEX IDX_SND_PROJECTS_OBJECTID ON snd.Projects(ObjectId);
GO

/*==============================================================*/
/* Table: ProjectItems                                          */
/*==============================================================*/
CREATE TABLE snd.ProjectItems (
   ProjectItemId        INTEGER              IDENTITY,
   ParentObjectId       UNIQUEIDENTIFIER,
   SuperPkgId           INTEGER              NOT NULL,
   Active               BIT                  NOT NULL DEFAULT 1,
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_PROJECTITEMS PRIMARY KEY (ProjectItemId),
   CONSTRAINT FK_SND_PROJECTITEMS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_PROJECTITEMS_SUPERPKGID FOREIGN KEY (SuperPkgId) REFERENCES snd.SuperPkgs (SuperPkgId),
   CONSTRAINT FK_SND_PROJECTITEMS_PARENTOBJECTID FOREIGN KEY (ParentObjectId) REFERENCES snd.Projects (ObjectId)

)
GO

CREATE INDEX IDX_SND_PROJECTITEMS_CONTAINER ON snd.ProjectItems(Container);
CREATE INDEX IDX_SND_PROJECTITEMS_SUPERPKGID ON snd.ProjectItems(SuperPkgId);
CREATE INDEX IDX_SND_PROJECTITEMS_PARENTOBJECTID ON snd.ProjectItems(ParentObjectId);
GO

/*==============================================================*/
/* Table: Events                                                */
/*==============================================================*/
CREATE TABLE snd.Events (
   EventId              INTEGER              NOT NULL,
   Id                   NVARCHAR(32)         NOT NULL,
   ParentObjectId       UNIQUEIDENTIFIER,
   Date                 DATETIME             NOT NULL,
   QcState              int,
   ObjectId             UNIQUEIDENTIFIER     NOT NULL DEFAULT newid(),
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_EVENTS PRIMARY KEY (EventId),
   CONSTRAINT FK_SND_EVENTS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_EVENTS_QCSTATE FOREIGN Key (QcState) REFERENCES core.QCState (RowId),
   CONSTRAINT FK_SND_EVENTS_PARENTOBJECTID FOREIGN KEY (ParentObjectId) REFERENCES snd.Projects (ObjectId)

)
GO

CREATE INDEX IDX_SND_EVENTS_CONTAINER ON snd.Events(Container);
CREATE INDEX IDX_SND_EVENTS_QCSTATE ON snd.Events(QcState);
CREATE INDEX IDX_SND_EVENTS_PARENTOBJECTID ON snd.Events(ParentObjectId);
GO

/*==============================================================*/
/* Table: CodedEvents                                           */
/*==============================================================*/
CREATE TABLE snd.CodedEvents (
   CodedEventId         INTEGER              IDENTITY,
   EventId              INTEGER              NOT NULL,
   SuperPkgId           INTEGER              NOT NULL,
   ObjectId             UNIQUEIDENTIFIER     NULL DEFAULT newid(),
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_CODEDEVENTS PRIMARY KEY (CodedEventId),
   CONSTRAINT FK_SND_CODEDEVENTS_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_CODEDEVENTS_EVENTID FOREIGN KEY (EventId) REFERENCES snd.Events (EventId),
   CONSTRAINT FK_SND_CODEDEVENTS_SUPERPKGID FOREIGN KEY (SuperPkgId) REFERENCES snd.SuperPkgs(SuperPkgId)

)
GO

CREATE INDEX IDX_SND_CODEDEVENTS_CONTAINER ON snd.CodedEvents(Container);
CREATE INDEX IDX_SND_CODEDEVENTS_EVENTID ON snd.CodedEvents(EventId);
CREATE INDEX IDX_SND_CODEDEVENTS_SUPERPKGID ON snd.CodedEvents(SuperPkgId);
GO

/*==============================================================*/
/* Table: EventNotes                                            */
/*==============================================================*/
CREATE TABLE snd.EventNotes (
   EventNoteId          INTEGER              IDENTITY,
   EventId              INTEGER,
   Note                 NVARCHAR(MAX)        NOT NULL,
   Container			      ENTITYID			       NOT NULL,
   CreatedBy			      USERID,
   Created				      DATETIME,
   ModifiedBy			      USERID,
   Modified				      DATETIME,
   Lsid                 LSIDType,

   CONSTRAINT PK_SND_EVENTNOTES PRIMARY KEY (EventNoteId),
   CONSTRAINT FK_SND_EVENTNOTES_CONTAINER FOREIGN KEY (Container) REFERENCES core.Containers (EntityId),
   CONSTRAINT FK_SND_EVENTNOTES_EVENTNOTEID FOREIGN KEY (EventNoteId) REFERENCES snd.Events (EventId)

)
GO

CREATE INDEX IDX_SND_EVENTNOTES_CONTAINER ON snd.EventNotes(Container);
CREATE INDEX IDX_SND_EVENTNOTES_EVENTNOTEID ON snd.EventNotes(EventNoteId);
GO

/* snd-17.21-17.22.sql */

CREATE INDEX IDX_SND_PKGS_LSID ON snd.Pkgs(Lsid);
CREATE INDEX IDX_SND_SUPERPKGS_LSID ON snd.SuperPkgs(Lsid);
CREATE INDEX IDX_SND_PKGCATEGORIES_LSID ON snd.PkgCategories(Lsid);
CREATE INDEX IDX_SND_PKGCATEGORYJUNCTION_LSID ON snd.PkgCategoryJunction(Lsid);
CREATE INDEX IDX_SND_PROJECTS_LSID ON snd.Projects(Lsid);
CREATE INDEX IDX_SND_PROJECTITEMS_LSID ON snd.ProjectItems(Lsid);
CREATE INDEX IDX_SND_EVENTS_LSID ON snd.Events(Lsid);
CREATE INDEX IDX_SND_CODEDEVENTS_LSID ON snd.CodedEvents(Lsid);
CREATE INDEX IDX_SND_EVENTNOTES_LSID ON snd.EventNotes(Lsid);

ALTER TABLE snd.Pkgs ADD Narrative NVARCHAR(MAX) NULL;

/* snd-17.22-17.23.sql */

/*==============================================================*/
/* Table: LookupSets                                            */
/*==============================================================*/
CREATE TABLE snd.LookupSets (
   LookupSetId          INTEGER              NOT NULL,
   SetName              NVARCHAR(128)        NOT NULL,
   Label                NVARCHAR(128),
   Description          NVARCHAR(900),
   Container			      ENTITYID			       NOT NULL,
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
   LookupSetId          INTEGER              NOT NULL,
   Value                VARCHAR(896)         NOT NULL,
   Displayable          BIT                  NOT NULL,
   SortOrder            INTEGER,
   Container			      ENTITYID			       NOT NULL,
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

ALTER TABLE snd.SuperPkgs ADD SortOrder INTEGER;

/* snd-17.23-17.24.sql */

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

/* snd-17.24-17.25.sql */

CREATE FUNCTION snd.fGetSuperPkg ( @TopLevelPkgId INT )
RETURNS TABLE
AS

-- ==========================================================================================
-- Author:			  Terry Hawkins
-- Creation date:	9/22/2017
-- Description:  Table valued function to return hierarchical view of a superPkg
-- ==========================================================================================
RETURN
    (
	WITH    CTE1 ( TopLevelPkgId, SuperPkgId, ParentSuperPkgId, PkgId, TreePath, SuperPkgPath, SortOrder, DESCRIPTION, Narrative, Active, Repeatable, Level )
              AS (
		
   -- anchor member definition
                   SELECT   @TopLevelPkgId AS TopLevelPkgId ,
                            sp.SuperPkgId AS SuperPkgId ,
                            sp.ParentSuperPkgId AS ParentSuperPkgId ,
                            sp.PkgId AS PkgId ,
                            RIGHT(SPACE(3)
                                  + CONVERT(VARCHAR(MAX), ROW_NUMBER() OVER ( ORDER BY sp.SortOrder )),
                                  3) AS TreePath ,
                            sp.SuperPkgPath AS SuperPkgPath ,
                            sp.SortOrder AS SortOrder ,
                            p.Description AS Description ,
                            p.Narrative AS Narrative ,
                            p.Active AS Active ,
                            p.Repeatable AS Repeatable ,
                            1 AS Level
                   FROM     snd.SuperPkgs sp
                            INNER JOIN snd.Pkgs p ON sp.PkgId = p.PkgId
                   WHERE    sp.PkgId = @TopLevelPkgId
                            AND sp.ParentSuperPkgId IS NULL
                   UNION ALL
                   SELECT   @TopLevelPkgId AS TopLevelPkgId ,
                            sp.SuperPkgId AS SuperPkgId ,
                            c.SuperPkgId AS ParentSuperPkgId ,
                            sp.PkgId AS PkgId ,
                            c.TreePath + '/' + RIGHT(SPACE(3)
                                                     + CONVERT(VARCHAR(MAX), RIGHT(ROW_NUMBER() OVER ( ORDER BY sp.SortOrder ),
                                                              10)), 3) AS TreePath ,
                            sp.SuperPkgPath AS SuperPkgPath ,
                            sp.SortOrder AS SortOrder ,
                            p.Description AS Description ,
                            p.Narrative AS Narrative ,
                            p.Active AS Active ,
                            p.Repeatable AS Repeatable ,
                            c.Level + 1 AS Level
                   FROM     snd.SuperPkgs AS sp
                            INNER JOIN CTE1 AS c ON 
								-- add subpackages
								sp.ParentSuperPkgId = c.SuperPkgId
								-- add children of subpackages
                                OR sp.ParentSuperPkgId IN (SELECT  sp2.SuperPkgId FROM snd.SuperPkgs AS sp2 WHERE c.PkgId = sp2.PkgId AND sp2.ParentSuperPkgId IS NULL )

                            INNER JOIN snd.Pkgs AS p ON sp.PkgId = p.PkgId
                 )
    SELECT  @TopLevelPkgId AS TopLevelPkgId ,
            c.SuperPkgId AS SuperPkgId ,
            c.ParentSuperPkgId AS ParentSuperPkgId ,
            c.PkgId AS PkgId ,
            c.TreePath AS TreePath ,
            c.SuperPkgPath AS SuperPkgPath ,
            c.SortOrder AS SortOrder ,
            c.DESCRIPTION AS Description ,
            c.Narrative AS Narrative ,
            c.Active AS Active ,
            c.Repeatable AS Repeatable ,
            c.Level AS Level
    FROM    CTE1 c
		
);
GO

/* snd-17.25-17.26.sql */

ALTER TABLE snd.PkgCategories ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();

ALTER TABLE snd.PkgCategoryJunction ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();

ALTER TABLE snd.ProjectItems ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();

/* snd-17.26-17.27.sql */

ALTER TABLE snd.Lookups ADD LookupId INT IDENTITY(1,1)
ALTER TABLE snd.Lookups DROP CONSTRAINT PK_SND_LOOKUPS
GO

ALTER TABLE snd.Lookups ADD CONSTRAINT PK_SND_LOOKUPS PRIMARY KEY (LookupId)

CREATE UNIQUE INDEX IDX_SND_LOOKUPS_LOOKUPSETID_VALUE ON snd.Lookups(LookupSetId, Value)