import { deleteRows, insertRows, updateRows } from '../../../query/actions'
import { EditableQueryModel, QueryColumn, QueryModel } from '../../../query/model'
import { setAppError } from '../../App/actions'

import { CAT_SQ } from '../../Packages/constants'

export function saveCategoryChanges(editableModel: EditableQueryModel, queryModel: QueryModel, values: {[key: string]: any}) {
    return (dispatch) => {
        const changes = getChanges(editableModel, queryModel, values);

        if (changes.added && changes.added.length) {
            insertRows(CAT_SQ.schemaName, CAT_SQ.queryName, changes.added).then((response) => {
                console.log('Insert Response', response);
            }).catch((error) => {
                console.log('Category Insert Error', error);
                dispatch(setAppError(error));
            });
        }

        if (changes.edited && changes.edited.length) {
            updateRows(CAT_SQ.schemaName, CAT_SQ.queryName, changes.edited).then((response) => {
                console.log('Update Response', response);
            }).catch((error) => {
                console.log('Category Insert Error', error);
                dispatch(setAppError(error));
            });
        }

        // add modal check for delete rows?

        if (changes.removed && changes.removed.length) {
            deleteRows(CAT_SQ.schemaName, CAT_SQ.queryName, changes.removed).then((response) => {
                console.log('Update Response', response);
            }).catch((error) => {
                console.log('Category Insert Error', error);
                dispatch(setAppError(error));
            });
        }
    }
}

function getChanges(editableModel: EditableQueryModel, queryModel: QueryModel, values: {[key: string]: any}): {
    added: Array<{[key: string]: any}>
    edited: Array<{[key: string]: any}>
    removed: Array<{[key: string]: any}>
} {
    const initialValues = queryModel.data;
    const editableIds = editableModel.dataIds;
    const initialIds = queryModel.dataIds;

    let added = [],
        edited = [],
        removed = getRemoved(initialIds, editableIds, editableModel.metaData.id);

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

    added = added.map((row) => {

        // req should be enforced in the grid itself
        return editableModel.getRequiredInsertColumns().reduce((prev, next: QueryColumn) => {
            const name = next.name,
                type = next.jsonType,
                hasValue = row[name] !== undefined;

            if (hasValue) {
                prev[name] = row[name]['value'];
            }
            else if (!hasValue && type === 'boolean') {
                prev[name] = false;
            }

            return prev;
        }, {});
    });

    edited = edited.map((row) => {
        return Object.keys(row).reduce((prev, next) => {
            if (row && row[next]) {
                prev[next] = row[next]['value'];
            }
            return prev;
        }, {});
    });

    return {
        added,
        edited,
        removed
    }
}

function getRemoved(initial: Array<number>, current: Array<number>, pkCol: string) {
    if (initial.length && current.length < initial.length) {
        return initial.filter((id: number) => {
            return current.indexOf(id) === -1;
        }).map(id => {
            return {
                [pkCol]: id
            };
        });
    }

    return [];
}