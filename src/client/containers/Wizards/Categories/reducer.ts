import { handleActions } from 'redux-actions';

import { CAT_WIZARD_TYPES } from './constants'
import { CategoryWizardContainer } from './model'

export const packages = handleActions({

    [CAT_WIZARD_TYPES.CATEGORIES_INIT]: (state: CategoryWizardContainer, action: any) => {
        //const { model } = action;

        return state;
    },


}, new CategoryWizardContainer());


// import { QUERY_TYPES } from './constants'
// import { handleActions } from 'redux-actions';
//
// import {
//     EditableQueryModel,
//     QueryModel,
//     QueryModelsContainer,
//     SchemaQuery
// } from './model'
//
// export const queries = handleActions({
//
//     [QUERY_TYPES.QUERY_ERROR]: (state: QueryModelsContainer, action: any) => {
//         const { error, schemaQuery } = action;
//
//
//         return state;
//     },
//
//     [QUERY_TYPES.QUERY_INIT]: (state: QueryModelsContainer, action: any) => {
//         const { props, schemaQuery } = action;
//
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         const model = new QueryModel({
//             schema: schemaQuery.schemaName,
//             query: schemaQuery.queryName,
//             view: schemaQuery.viewName
//         });
//
//         const models: {[key: string]: QueryModel} = Object.assign({}, state.models,{[schemaQuery.resolveKey()]: model});
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel({schemaQuery, ...props});
//             editableModels = Object.assign({}, editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//         const updatedState = Object.assign({}, state, {editableModels, models});
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_INVALIDATE]: (state: QueryModelsContainer, action: any) => {
//         const { schemaQuery } = action;
//         const { loadedQueries, loadingQueries } = state;
//
//         let loaded = removeQuery(schemaQuery, loadedQueries),
//             loading = removeQuery(schemaQuery, loadingQueries);
//
//         let models = Object.assign({}, state.models),
//             editableModels = Object.assign({}, state.editableModels);
//
//         delete models[schemaQuery.resolveKey()];
//         delete editableModels[schemaQuery.resolveKey()];
//
//         const updatedState = Object.assign({}, state, {
//             editableModels,
//             loadedQueries: loaded,
//             loadingQueries: loading,
//             models
//         });
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_LOADED]: (state: QueryModelsContainer, action: any) => {
//         const { schemaQuery } = action;
//         const { loadedQueries, loadingQueries } = state;
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         let loaded = addQuery(schemaQuery, loadedQueries),
//             loading = removeQuery(schemaQuery, loadingQueries);
//
//         const model = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
//             isLoaded: true,
//             isLoading: false
//         }));
//
//         const models = Object.assign({}, state.models, {[schemaQuery.resolveKey()]: model});
//
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel(Object.assign({}, state.editableModels[schemaQuery.resolveKey()], {
//                 isLoaded: true,
//                 isLoading: false
//             }));
//             editableModels = Object.assign(editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//         const updatedState = Object.assign({}, state, {
//             editableModels,
//             loadedQueries: loaded,
//             loadingQueries: loading,
//             models,
//         });
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_LOADING]: (state: QueryModelsContainer, action: any) => {
//         const { schemaQuery } = action;
//         const { loadedQueries, loadingQueries } = state;
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         let loaded = removeQuery(schemaQuery, loadedQueries),
//             loading = addQuery(schemaQuery, loadingQueries);
//
//         const model = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
//             isLoaded: false,
//             isLoading: true
//         }));
//
//         const models = Object.assign({}, state.models, {[schemaQuery.resolveKey()]: model});
//
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel(Object.assign({}, state.editableModels[schemaQuery.resolveKey()], {
//                 isLoaded: false,
//                 isLoading: true
//             }));
//             editableModels = Object.assign(editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//         const updatedState = Object.assign({}, state, {
//             editableModels,
//             loadedQueries: loaded,
//             loadingQueries: loading,
//             models,
//         });
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_SUCCESS]: (state: QueryModelsContainer, action: any) => {
//         const { response, schemaQuery } = action;
//         const pkCol = response.metaData.id;
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         let dataIds = [];
//         const data = response.rows.reduce((prev, next) => {
//
//             const id = next[pkCol].value;
//             dataIds.push(id);
//             prev[id] = next;
//
//             return prev;
//
//         }, {});
//
//         const queryModel = new QueryModel(Object.assign({}, state.models[schemaQuery.resolveKey()], {
//             data,
//             dataIds,
//             dataCount: response.rowCount,
//             metaData: response.metaData
//         }));
//
//         const models = Object.assign({}, state.models,{[schemaQuery.resolveKey()]: queryModel});
//
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel(Object.assign({}, state.editableModels[schemaQuery.resolveKey()], {
//                 data,
//                 dataIds,
//                 dataCount: response.rowCount,
//                 metaData: response.metaData
//             }));
//             editableModels = Object.assign(editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//
//         const updatedState = Object.assign({}, state, { editableModels, models });
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_EDIT_ADD_ROW]: (state: QueryModelsContainer, action: any) => {
//         const { schemaQuery } = action;
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         let dataIds = [].concat(state.editableModels[schemaQuery.resolveKey()].dataIds);
//         const largest = dataIds.sort()[dataIds.length - 1];
//         dataIds.push(largest + 1);
//
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel(Object.assign({}, state.editableModels[schemaQuery.resolveKey()], {
//                 dataIds,
//                 dataCount: dataIds.length,
//             }));
//             editableModels = Object.assign(editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//         const updatedState = Object.assign({}, state, {editableModels});
//         return new QueryModelsContainer(updatedState);
//     },
//
//     [QUERY_TYPES.QUERY_EDIT_REMOVE_ROW]: (state: QueryModelsContainer, action: any) => {
//         const { rowId, schemaQuery } = action;
//
//         let editableModel: EditableQueryModel,
//             editableModels: {[key: string]: EditableQueryModel} = Object.assign({}, state.editableModels);
//
//         let dataIds = [].concat(state.editableModels[schemaQuery.resolveKey()].dataIds);
//         const index = dataIds.findIndex(d => {
//             return d === rowId
//         });
//
//         dataIds.splice(index, 1);
//
//         if (schemaQuery.params && schemaQuery.params.editable === true) {
//             editableModel = new EditableQueryModel(Object.assign({}, state.editableModels[schemaQuery.resolveKey()], {
//                 dataIds,
//                 dataCount: dataIds.length,
//             }));
//             editableModels = Object.assign(editableModels,{[schemaQuery.resolveKey()]: editableModel});
//         }
//
//         const updatedState = Object.assign({}, state, {editableModels});
//         return new QueryModelsContainer(updatedState);
//     },
//
// }, new QueryModelsContainer());
//
//
// function addQuery(schemaQuery: SchemaQuery, queries: Array<string>): Array<string> {
//     if (queries && Array.isArray(queries) && queries.indexOf(schemaQuery.resolveKey()) === -1) {
//         return queries.concat(schemaQuery.resolveKey());
//     }
//
//     return [];
// }
//
// function removeQuery(schemaQuery: SchemaQuery, queries: Array<string>): Array<string> {
//     if (queries && Array.isArray(queries) && queries.indexOf(schemaQuery.resolveKey()) !== -1) {
//         return queries.filter((q) => q !== schemaQuery.resolveKey());
//     }
//
//     return queries;
// }