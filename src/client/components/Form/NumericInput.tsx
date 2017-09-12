import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface NumericInputProps {
    disabled?: boolean
    required?: boolean
}

export const NumericInput = (field: WrappedFieldProps<{}> & NumericInputProps) => {
    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required={field.disabled === true}
                type="number"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};