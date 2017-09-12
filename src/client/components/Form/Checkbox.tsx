import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface CheckboxInputProps {
    disabled?: boolean
    required?: boolean
}

export const CheckboxInput = (field: WrappedFieldProps<any> & CheckboxInputProps) => {
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