import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface LookupKeyInputProps {
    attribute?: any
    attributeId?: any
    disabled?: boolean
    required?: boolean
}

export const LookupKeyInput = (field: WrappedFieldProps<{}> & LookupKeyInputProps) => {
    let value = '';
    const schema = field.attribute['lookupSchema' + field.attributeId],
        query = field.attribute['lookupQuery' + field.attributeId];

    if (field.input.value) {
        value = field.input.value;
    }
    else if (schema && query){
        value = [schema, query].join('.');
    }

    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required={field.required === true}
                type="text"
                value={value}/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};