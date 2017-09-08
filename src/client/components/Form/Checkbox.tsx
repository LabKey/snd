import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

export const CheckboxInput = (field: WrappedFieldProps<any>) => {
    const checked = field.input.value;
    return (
        <span className="input-row">
            <input
                {...field.input}
                checked={checked}
                required
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