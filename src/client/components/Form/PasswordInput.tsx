import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

export const PasswordInput = (field: WrappedFieldProps<any>) => {
    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                name="password"
                required
                type="password"/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};