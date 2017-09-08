import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

export const EmailInput = (field: WrappedFieldProps<any>) => {
    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                name="email"
                required
                type="email"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};