import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface FieldTextInputProps {
    disabled?: boolean
    required?: boolean
}

export const FieldTextInput = (field: WrappedFieldProps<{}> & FieldTextInputProps) => {
    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required={field.required === true}
                type="text"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};


interface TextInputProps {
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLInputElement>>
    required?: boolean
    value?: string
}

export const TextInput = (props: TextInputProps) => {
    return (
        <div className="input-row">
            <input
                className="form-control"
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.required === true}
                type="text"
                value={props.value ? props.value : ''}/>
        </div>
    );
};