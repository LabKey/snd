import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface LookupKeyInputProps {
    disabled?: boolean
    attribute?: any
    attributeId?: any
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
                required
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