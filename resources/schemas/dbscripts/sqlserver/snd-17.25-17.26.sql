ALTER TABLE snd.PkgCategories ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();

ALTER TABLE snd.PkgCategoryJunction ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();

ALTER TABLE snd.ProjectItems ADD Objectid uniqueidentifier NOT NULL DEFAULT newid();