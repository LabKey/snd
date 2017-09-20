import { CAT_WIZARD_TYPES } from './constants'

import { insertRows, updateRows } from '../../../query/actions'

export function saveCategoryChanges(model: any, values: {[key: string]: any}) {
    const initialValues = model.data;

    let added = [],
        edited = [];

    Object.keys(values).forEach((key) => {
        const row = values[key];
        const initialRow = initialValues[key];

        if (initialRow) {
            const hasEdits = Object.keys(row).some((field) => {
                if (row[field] && row[field]['value'] && initialRow[field] && initialRow[field]['value']) {
                    return row[field]['value'] !== initialRow[field]['value'];
                }

                return false;
            });

            hasEdits ? edited.push(row) : null;
        }
        else {
            added.push(values[key])
        }
    });

    console.log(edited)
    console.log(added)





}

export function initPackageModel(id, props?: {[key: string]: any}) {
    return {
        type: CAT_WIZARD_TYPES.CATEGORIES_INIT,
        id,
        props
    }
}




//
// export function queryEditRemoveRow(schemaQuery: SchemaQuery, rowId: number) {
//     return {
//         type: QUERY_TYPES.QUERY_EDIT_REMOVE_ROW,
//         rowId,
//         schemaQuery
//     };
// }
//
// export function queryEditAddRow(schemaQuery: SchemaQuery) {
//     return {
//         type: QUERY_TYPES.QUERY_EDIT_ADD_ROW,
//         schemaQuery
//     };
// }