import { QUERY_TYPES } from './constants'
import { LabKeyQueryResponse, QueryModel, SchemaQuery } from './model'

export function queryError(schemaQuery: SchemaQuery, error: any) {
    return {
        type: QUERY_TYPES.QUERY_ERROR,
        error,
        schemaQuery
    };
}

function shouldSearch(model: QueryModel): boolean {

    const { dataCount, isLoaded, isLoading } = model;

    return !dataCount && !isLoaded && !isLoading;
}

export function queryInitialize(schemaQuery: SchemaQuery): (dispatch, getState) => any {
    return (dispatch, getState: () => APP_STATE_PROPS) => {
        let model: QueryModel = getState().queries.models[schemaQuery.resolveKey()];

        if (!model) {
            dispatch({
                type: QUERY_TYPES.QUERY_INIT,
                schemaQuery
            });
        }

        model = getState().queries.models[schemaQuery.resolveKey()];

        if (shouldSearch(model)) {
            dispatch(querySelectRows(schemaQuery.schemaName, schemaQuery.queryName, schemaQuery.viewName));
        }
    }
}

export function queryInvalidate(schemaQuery: SchemaQuery) {
    return {
        type: QUERY_TYPES.QUERY_INVALIDATE,
        schemaQuery
    };
}

export function queryLoaded(schemaQuery: SchemaQuery) {
    return {
        type: QUERY_TYPES.QUERY_LOADED,
        schemaQuery
    };
}

export function queryLoading(schemaQuery: SchemaQuery) {
    return {
        type: QUERY_TYPES.QUERY_LOADING,
        schemaQuery
    };
}

export function querySuccess(schemaQuery: SchemaQuery, response) {
    return {
        type: QUERY_TYPES.QUERY_SUCCESS,
        response,
        schemaQuery
    };
}

export function querySelectRows(schemaName: string, queryName: string, viewName?: string, params?: {[key: string]: any}) {
    return (dispatch, getState: () => APP_STATE_PROPS) => {
        const schemaQuery: SchemaQuery = SchemaQuery.create(schemaName, queryName, viewName);
        const model = getState().queries.models[schemaQuery.resolveKey()];

        if (!model) {
            dispatch(queryInitialize(schemaQuery));
        }

        dispatch(queryLoading(schemaQuery));

        return selectRows(schemaName, queryName, viewName, params).then((response: LabKeyQueryResponse) => {

           const { id } = response.metaData;
           if (id) {
                dispatch(querySuccess(schemaQuery, response));
                dispatch(queryLoaded(schemaQuery));
           }
           else {
               throw new Error([schemaName, queryName, viewName].join(' ') + 'Response does not include id column');
           }

        }).catch((error) => {
            dispatch(queryError(schemaQuery, error));
        });
    }
}


export function labkeyAjax(controller: string, action: string, params?: any, jsonData?: any, container?: string): Promise<any> {
    return new Promise((resolve, reject) => {
        return LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL(controller, [action, 'api'].join('.'), container),
            jsonData,
            params,
            success: LABKEY.Utils.getCallbackWrapper((data) => {
                resolve(data);
            }),
            failure: LABKEY.Utils.getCallbackWrapper((data) => {
                reject(data);
            })
        });
    });
}

export function selectRows(schemaName: string, queryName: string, viewName?: string, params?: {[key: string]: any}): Promise<any> {
    return new Promise((resolve, reject) => {
        return LABKEY.Query.selectRows({
            schemaName,
            queryName,
            viewName,
            params,
            requiredVersion: 17.1, // newer?
            success: (data: LabKeyQueryResponse) => {
                resolve(data);
            },
            failure: (data) => {
                reject(data);
            }
        });
    });
}

export function deleteRows(schemaName: string, queryName: string, rows: Array<{[key: string]: any}>) {
    return new Promise((resolve, reject) => {
        LABKEY.Query.deleteRows({
            schemaName,
            queryName,
            rows,
            success: (data: LabKeyQueryResponse) => {
                resolve(data);
            },
            failure: (data) => {
                reject(data);
            }
        });
    });
};