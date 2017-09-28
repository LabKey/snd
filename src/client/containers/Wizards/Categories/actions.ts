import { deleteRows, insertRows, queryInvalidate, updateRows } from '../../../query/actions'
import { EditableQueryModel, QueryColumn, QueryModel } from '../../../query/model'
import { setAppError, setAppMessage } from '../../App/actions'

import { CAT_SQ } from '../../Packages/constants'

interface CategoryActionProps {
    action: (schemaName: string, queryName: string, rows: Array<any>) => any
    schemaName: string
    queryName: string
    rows: Array<any>
}
export function saveCategoryChanges(editableModel: EditableQueryModel, queryModel: QueryModel, values: {[key: string]: any}) {
    return (dispatch, getState: () => APP_STATE_PROPS) => {
        const changes = getChanges(editableModel, queryModel, values);

        dispatch(editableModel.setSubmitting());

        function handleActions(actionArr: Array<CategoryActionProps>) {
            if (actionArr.length === 0) {
                return Promise.resolve();
            }

            const nextAction: CategoryActionProps = actionArr[0];
            const { action, queryName, schemaName, rows } = nextAction;

            const remaining: Array<CategoryActionProps> = actionArr.slice(1);
            return action(schemaName, queryName, rows).catch((error) => {
                dispatch(setAppError(error));

                const editModel = getState().queries.editableModels[editableModel.id];
                dispatch(editModel.setSubmitted());
            }).then(() => {
                return handleActions(remaining);
            });
        }

        let hasAdded = changes.added && changes.added.length,
            hasEdited = changes.edited && changes.edited.length,
            hasRemoved =changes.removed && changes.removed.length;

        let actions: Array<CategoryActionProps> = [];

        if (hasAdded) {
            const addCategories: CategoryActionProps = {
                action: insertRows,
                schemaName: CAT_SQ.schemaName,
                queryName: CAT_SQ.queryName,
                rows: changes.added
            };

            actions.push(addCategories);
        }

        if (hasEdited) {
            const editCategories: CategoryActionProps = {
                action: updateRows,
                schemaName: CAT_SQ.schemaName,
                queryName: CAT_SQ.queryName,
                rows: changes.edited
            };

            actions.push(editCategories);
        }

        // add modal check for delete rows?

        if (hasRemoved) {
            const deleteCategories: CategoryActionProps = {
                action: deleteRows,
                schemaName: CAT_SQ.schemaName,
                queryName: CAT_SQ.queryName,
                rows: changes.removed
            };

            actions.push(deleteCategories);
        }

        return handleActions(actions).then(() => {
            let editModel = getState().queries.editableModels[editableModel.id];
            dispatch(editModel.setSubmitted());
            dispatch(queryInvalidate(CAT_SQ));

            dispatch(setAppMessage('Changes saved'));

            setTimeout(() => {
                dispatch(setAppMessage(''));
            }, 2000);

        });
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