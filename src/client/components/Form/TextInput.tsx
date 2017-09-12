import * as React from 'react';
import { WrappedFieldProps, formValueSelector } from 'redux-form';

interface TextInputProps {
    disabled?: boolean
    required?: boolean
}

export const TextInput = (field: WrappedFieldProps<{}> & TextInputProps) => {
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

export const OtherInput = (field: WrappedFieldProps<{}> & TextInputProps) => {
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