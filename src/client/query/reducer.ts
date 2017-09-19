import { QUERY_TYPES } from './constants'
import { handleActions } from 'redux-actions';

import {
    QueryModel,
    QueryModelsContainer,
    SchemaQuery
} from './model'

export const queries = handleActions({

    [QUERY_TYPES.QUERY_ERROR]: (state: QueryModelsContainer, action: any) => {
        const { error, schemaQuery } = action;


        return state;
    },

    [QUERY_TYPES.QUERY_INIT]: (state: QueryModelsContainer, action: any) => {
        const { schemaQuery } = action;

        const model = new QueryModel({
            schema: schemaQuery.schemaName,
            query: schemaQuery.queryName,
            view: schemaQuery.viewName
        });

        const models = Object.assign({}, state.models,{[schemaQuery.resolveKey()]: model});

        const updatedState = Object.assign({}, state, {models});
        return new QueryModelsContainer(updatedState);
    },

    [QUERY_TYPES.QUERY_INVALIDATE]: (state: QueryModelsContainer, action: any) => {
        const { schemaQuery } = action;
        const { loadedQueries, loadingQueries } = state;

        let loaded = removeQuery(schemaQuery, loadedQueries),
            loading = removeQuery(schemaQuery, loadingQueries);

        const updatedState = Object.assign({}, state, {loadedQueries: loaded, loadingQueries: loading});
        return new QueryModelsContainer(updatedState);
    },

    [QUERY_TYPES.QUERY_LOADED]: (state: QueryModelsContainer, action: any) => {
        const { schemaQuery } = action;
        const { loadedQueries, loadingQueries } = state;

        let loaded = addQuery(schemaQuery, loadedQueries),
            loading = removeQuery(schemaQuery, loadingQueries);

        const model = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
            isLoaded: true,
            isLoading: false
        }));

        const updatedModels = Object.assign({}, state.models, {[schemaQuery.resolveKey()]: model});

        const updatedState = Object.assign({}, state, {
            models: updatedModels,
            loadedQueries: loaded,
            loadingQueries: loading
        });
        return new QueryModelsContainer(updatedState);
    },

    [QUERY_TYPES.QUERY_LOADING]: (state: QueryModelsContainer, action: any) => {
        const { schemaQuery } = action;
        const { loadedQueries, loadingQueries } = state;

        let loaded = removeQuery(schemaQuery, loadedQueries),
            loading = addQuery(schemaQuery, loadingQueries);

        const model = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
            isLoaded: false,
            isLoading: true
        }));

        const updatedModels = Object.assign({}, state.models, {[schemaQuery.resolveKey()]: model});

        const updatedState = Object.assign({}, state, {
            models: updatedModels,
            loadedQueries: loaded,
            loadingQueries: loading
        });
        return new QueryModelsContainer(updatedState);
    },

    [QUERY_TYPES.QUERY_SUCCESS]: (state: QueryModelsContainer, action: any) => {
        const { response, schemaQuery } = action;
        const pkCol = response.metaData.id;

        let dataIds = [];
        const data = response.rows.reduce((prev, next) => {

            const id = next[pkCol].value;
            dataIds.push(id);
            prev[id] = next;

            return prev;

        }, {});

        const queryModel = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
            data,
            dataIds,
            dataCount: response.rowCount,
            metaData: response.metaData
        }));

        const models = Object.assign({}, state.models,{[schemaQuery.resolveKey()]: queryModel});

        const updatedState = Object.assign({}, state, {models});
        return new QueryModelsContainer(updatedState);
    },

}, new QueryModelsContainer());


function addQuery(schemaQuery: SchemaQuery, queries: Array<string>): Array<string> {
    if (queries && Array.isArray(queries) && queries.indexOf(schemaQuery.resolveKey()) === -1) {
        return queries.concat(schemaQuery.resolveKey());
    }

    return [];
}

function removeQuery(schemaQuery: SchemaQuery, queries: Array<string>): Array<string> {
    if (queries && Array.isArray(queries) && queries.indexOf(schemaQuery.resolveKey()) !== -1) {
        return queries.filter((q) => q !== schemaQuery.resolveKey());
    }

    return queries;
}