import { QUERY_TYPES } from './constants'
import { handleActions } from 'redux-actions';

import { QueryModel, QueryModelsContainer } from './model'
import * as actions from './actions'



export const queries = handleActions({

    [QUERY_TYPES.QUERY_ERROR]: (state: QueryModelsContainer, action: any) => {
        const { error, schemaQuery } = action;


        return state;
    },

    [QUERY_TYPES.QUERY_INIT]: (state: QueryModelsContainer, action: any) => {
        const { schemaQuery } = action;

        const model = new QueryModel({
            schema: actions.getSchemaName(schemaQuery),
            query: actions.getQueryName(schemaQuery)
        });

        const updatedState = Object.assign({}, state, {data: {[schemaQuery]: model}});
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

        const model = new QueryModel(Object.assign({}, state.data[schemaQuery], {
            isLoaded: true,
            isLoading: false
        }));

        const updatedData = Object.assign({}, state.data, {[schemaQuery]: model});

        const updatedState = Object.assign({}, state, {
            data: updatedData,
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

        const model = new QueryModel(Object.assign({}, state.data[schemaQuery], {
            isLoaded: false,
            isLoading: true
        }));

        const updatedData = Object.assign({}, state.data, {[schemaQuery]: model});

        const updatedState = Object.assign({}, state, {
            data: updatedData,
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

        const queryModel = new QueryModel(Object.assign({}, state.data[schemaQuery], {
            data,
            dataIds,
            dataCount: response.rowCount,
            metaData: response.metaData
        }));

        const updatedState = Object.assign({}, state, {data: {[schemaQuery]: queryModel}});
        return new QueryModelsContainer(updatedState);
    },

}, new QueryModelsContainer());


function addQuery(query, queries: Array<string>): Array<string> {
    if (queries && Array.isArray(queries) && queries.indexOf(query) === -1) {
        return queries.concat(query);
    }

    return [];
}

function removeQuery(query, queries: Array<string>): Array<string> {
    if (queries && Array.isArray(queries) && queries.indexOf(query) !== -1) {
        return queries.filter((q) => q !== query);
    }

    return queries;
}