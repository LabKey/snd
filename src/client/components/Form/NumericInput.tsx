import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

export const NumericInput = (field: WrappedFieldProps<{}> & {disabled?: boolean}) => {
    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required
                type="number"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};