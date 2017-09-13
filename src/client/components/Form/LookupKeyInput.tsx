import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface FieldLookupKeyInputProps {
    attribute?: any
    attributeId?: any
    disabled?: boolean
    required?: boolean
}

export const FieldLookupKeyInput = (field: WrappedFieldProps<{}> & FieldLookupKeyInputProps) => {
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
// add error check type behavior looking for schema.query format
interface LookupKeyInputProps {
    attribute?: any
    attributeId?: any
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLInputElement>>
    required?: boolean
    value?: string
}

export const LookupKeyInput = (props: LookupKeyInputProps) => {
    let value = '';
    const schema = props.attribute['lookupSchema' + props.attributeId],
        query = props.attribute['lookupQuery' + props.attributeId];

    if (props.value) {
        value = props.value;
    }
    else if (schema && query){
        value = [schema, query].join('.');
    }

    return (
        <div className="input-row">
            <input
                className="form-control"
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.required === true}
                type="text"
                value={value}/>
        </div>
    );
};