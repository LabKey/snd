import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface FieldNumericInputProps {
    disabled?: boolean
    required?: boolean
}

export const FieldNumericInput = (field: WrappedFieldProps<{}> & FieldNumericInputProps) => {
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

interface NumericInputProps {
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLInputElement>>
    required?: boolean
    value?: number
}

export const NumericInput = (props: NumericInputProps) => {
    return (
        <div className="input-row">
            <input
                className="form-control"
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.disabled === true}
                type="number"
                value={props.value}/>
        </div>
    );
};