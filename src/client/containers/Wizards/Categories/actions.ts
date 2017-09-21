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

