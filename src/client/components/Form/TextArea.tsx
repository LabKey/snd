import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface TextAreaProps {
    disabled?: boolean
    cols?: number
    required?: boolean
    rows?: number
}

export const TextArea = (field: WrappedFieldProps<{}> & TextAreaProps) => {
    return (
        <div className="input-row">
            <textarea
                {...field.input}
                className="form-control"
                cols={field.cols}
                disabled={field.disabled === true}
                onChange={(event) => field.input.onChange(event)}
                required={field.required === true}
                rows={field.rows}/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};