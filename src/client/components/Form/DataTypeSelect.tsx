import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

export const DataTypeSelect = (field: WrappedFieldProps<{}> & {disabled?: boolean}) => {
    return (
        <div className="input-row">
            <select
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required>
                <option value=""/>
                {DataTypeOptions.map((opt: string, i: number) => {
                    return <option key={i} value={opt}>{opt}</option>;
                })}
            </select>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};

const DataTypeOptions: Array<string> = [
    'String',
    'Integer',
    'Double'
];