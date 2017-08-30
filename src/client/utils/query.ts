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
            success: (data) => {
                resolve(data);
            },
            failure: (data) => {
                reject(data);
            }
        });
    });
}