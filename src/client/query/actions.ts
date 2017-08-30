import { QUERY_TYPES } from './constants'
import { LabKeyQueryResponse } from './model'


export function resolveKey(schema: string, query: string): string {
    return [schema, query].join('|').toLowerCase();
}

export function getSchemaName(schemaQuery: string) {
    if (schemaQuery) {
        const splitSchemaQuery = schemaQuery.split('|');
        if (splitSchemaQuery && splitSchemaQuery.length === 2) {
            return splitSchemaQuery[0];
        }
    }

    return undefined;
}

export function getQueryName(schemaQuery: string) {
    if (schemaQuery) {
        const splitSchemaQuery = schemaQuery.split('|');
        if (splitSchemaQuery && splitSchemaQuery.length === 2) {
            return splitSchemaQuery[1];
        }
    }

    return undefined;
}

export function queryError(schemaQuery: string, error: any) {
    return {
        type: QUERY_TYPES.QUERY_ERROR,
        error,
        schemaQuery
    };
}

export function queryInvalidate(schemaQuery: string) {
    return {
        type: QUERY_TYPES.QUERY_INVALIDATE,
        schemaQuery
    };
}

export function queryLoaded(schemaQuery: string) {
    return {
        type: QUERY_TYPES.QUERY_LOADED,
        schemaQuery
    };
}

export function queryLoading(schemaQuery: string) {
    return {
        type: QUERY_TYPES.QUERY_LOADING,
        schemaQuery
    };
}

export function querySuccess(schemaQuery: string, response) {
    return {
        type: QUERY_TYPES.QUERY_SUCCESS,
        response,
        schemaQuery
    };
}

export function querySelectRows(schemaName: string, queryName: string, params?: {[key: string]: any}) {
    return (dispatch) => {
        const schemaQuery: string = resolveKey(schemaName, queryName);
        dispatch(queryLoading(schemaQuery));

        return selectRows(schemaName, queryName, params).then((response: LabKeyQueryResponse) => {
           dispatch(queryLoaded(schemaQuery));

           const { id } = response.metaData;
           if (id) {
                dispatch(querySuccess(schemaQuery, response));
           }
           else {
               throw new Error([schemaName, queryName].join(' ') + 'Response does not include id column');
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

export function selectRows(schemaName: string, queryName: string, params?: {[key: string]: any}): Promise<any> {
    return new Promise((resolve, reject) => {
        return LABKEY.Query.selectRows({
            schemaName,
            queryName,
            ...params,
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