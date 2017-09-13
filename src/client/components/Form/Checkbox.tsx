import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface FieldCheckboxInputProps {
    disabled?: boolean
    required?: boolean
}

export const FieldCheckboxInput = (field: WrappedFieldProps<any> & FieldCheckboxInputProps) => {
    const checked = field.input.value;
    return (
        <span className="input-row">
            <input
                {...field.input}
                checked={checked}
                disabled={field.disabled === true}
                required={field.required === true}
                tabIndex={0}
                type="checkbox"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </span>
    );
};

interface CheckboxInputProps {
    checked?: boolean
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLInputElement>>
    required?: boolean
    value?: string
}

export const CheckboxInput = (props: CheckboxInputProps) => {
    return (
        <span className="input-row">
            <input
                checked={props.checked}
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.required === true}
                tabIndex={0}
                type="checkbox"/>
        </span>
    );
};