import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface DataTypeSelectProps {
    disabled?: boolean
    required?: boolean
}

export const DataTypeSelect = (field: WrappedFieldProps<{}> & DataTypeSelectProps) => {
    return (
        <div className="input-row">
            <select
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required={field.required === true}>
                <option value=""/>
                {DataTypeOptions.map((opt: {label: string, value: string}, i: number) => {
                    return <option key={i} value={opt.value}>{opt.label}</option>;
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

const DataTypeOptions: Array<{label: string, value: string}> = [
    {
        value: 'http://www.w3.org/2001/XMLSchema#string',
        label: 'String'
    },
    {
        value: 'http://www.w3.org/2001/XMLSchema#integer',
        label: 'Integer'
    },
    {
        value: 'http://www.w3.org/2001/XMLSchema#double',
        label: 'Double'
    }

];