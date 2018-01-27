
import { SchemaQuery } from '../../query/model'

const SUPER_PACKAGES_PREFIX = 'superpackages/';

export const SND_PKG_SCHEMA: string = 'snd',
    SND_TOP_LEVEL_SUPER_PKG_QUERY: string = 'topLevelSuperPkgs';

export const TOPLEVEL_SUPER_PKG_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_TOP_LEVEL_SUPER_PKG_QUERY);

const superPkgRequiredColumns = [
    'SuperPkgId',
    'ParentSuperPkgId',
    'PkgId',
    'SuperPkgPath',
    'SortOrder',
    'HasEvent',
    'HasProject',
    'IsPrimitive',
    'Container',
    'Created',
    'CreatedBy',
    'Modified',
    'ModifiedBy'
];

const topLevelSuperPkgRequiredColumns = [
    'SuperPkgId',
    'PkgId',
    'Description',
    'Narrative',
    'IsPrimitive',
    'Container',
    'Repeatable'
];

export const SUPERPKG_REQUIRED_COLUMNS = {
    SUPER_PKG: superPkgRequiredColumns,
    TOP_LEVEL_SUPER_PKG: topLevelSuperPkgRequiredColumns
};