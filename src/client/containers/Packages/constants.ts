import { SchemaQuery } from '../../query/model'

const PKG_PREFIX = 'packages/';

export const PKG_TYPES = {
    PACKAGES_ERROR: PKG_PREFIX + 'PACKAGES_ERROR',
    PACKAGES_INIT: PKG_PREFIX + 'PACKAGES_INIT',
    PACKAGES_INVALIDATE: PKG_PREFIX + 'PACKAGES_INVALIDATE',
    PACKAGES_SUCCESS: PKG_PREFIX + 'PACKAGES_SUCCESS',
    PACKAGES_SEARCH_FILTER: PKG_PREFIX + 'PACKAGES_SEARCH_FILTER',
    PACKAGES_TOGGLE_DRAFTS: PKG_PREFIX + 'PACKAGES_TOGGLE_DRAFTS',
    PACKAGES_WARNING: PKG_PREFIX + 'PACKAGES_WARNING',
    PACKAGES_RESET_WARNING: PKG_PREFIX + 'PACKAGES_RESET_WARNING',
    SET_ACTIVE_PACKAGE: PKG_PREFIX + 'SET_ACTIVE_PACKAGE',
};

export const SND_PKG_SCHEMA: string = 'snd',
    SND_PKG_QUERY: string = 'pkgs',
    SND_SUPER_PKG_QUERY: string = 'superPkgs',
    SND_CATEGORY_QUERY: string = 'pkgCategories';

export const CAT_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_CATEGORY_QUERY);
export const EDITABLE_CAT_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_CATEGORY_QUERY, undefined, {editable: true});
export const PKG_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_PKG_QUERY);
export const TOPLEVEL_SUPER_PKG_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_SUPER_PKG_QUERY, 'TopLevelSuperPkgs');

const categoriesRequiredColumns = [
    'CategoryId',
    'Description'
];

const packagesRequiredColumns = [
    'Active',
    'Container',
    'Created',
    'CreatedBy',
    'Description',
    'HasEvent',
    'HasProject',
    'ModifiedBy',
    'Narrative',
    'ObjectId',
    'PkgId',
    'QcState',
    'Repeatable',
    'links'
];

const superPkgRequiredColumns = [
    'container',
    'created',
    'createdBy',
    'modified',
    'modifiedBy',
    'narrative',
    'parentSuperPkgId',
    'superPkgPath',
    'superPkgId',
    'pkgId',
    'isPrimitive',
    'links'
];

export const REQUIRED_COLUMNS = {
    CATS: categoriesRequiredColumns,
    PKGS: packagesRequiredColumns,
    SUPER_PKG: superPkgRequiredColumns,
};