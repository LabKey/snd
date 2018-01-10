
import { SchemaQuery } from '../../query/model'

const PROJECT_PREFIX = 'projects/';

export const PROJECT_TYPES = {
    PROJECTS_ERROR: PROJECT_PREFIX + 'PROJECTS_ERROR',
    PROJECTS_INIT: PROJECT_PREFIX + 'PROJECTS_INIT',
    PROJECTS_INVALIDATE: PROJECT_PREFIX + 'PROJECTS_INVALIDATE',
    PROJECTS_SUCCESS: PROJECT_PREFIX + 'PROJECTS_SUCCESS',
    PROJECTS_SEARCH_FILTER: PROJECT_PREFIX + 'PROJECTS_SEARCH_FILTER',
    PROJECTS_TOGGLE_DRAFTS: PROJECT_PREFIX + 'PROJECTS_TOGGLE_DRAFTS',
    PROJECTS_WARNING: PROJECT_PREFIX + 'PROJECTS_WARNING',
    PROJECTS_RESET_WARNING: PROJECT_PREFIX + 'PROJECTS_RESET_WARNING',
    PROJECTS_RESET_FILTER: PROJECT_PREFIX + 'PROJECTS_RESET_FILTER',
    SET_ACTIVE_PROJECT: PROJECT_PREFIX + 'SET_ACTIVE_PROJECT',
};

const projectsRequiredColumns = [
    'Active',
    'Description',
    'EndDate',
    'HasEvent',
    'ObjectId',
    'ProjectId',
    'ReferenceId',
    'RevisionNum',
    'StartDate'
];

export const SND_PROJECT_SCHEMA: string = 'snd',
    SND_PROJECT_QUERY: string = 'Projects';

export const PROJECT_SQL = SchemaQuery.create(SND_PROJECT_SCHEMA, SND_PROJECT_QUERY);

export const REQUIRED_COLUMNS = {
    PROJECTS: projectsRequiredColumns
};