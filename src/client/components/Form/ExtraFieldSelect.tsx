import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface ExtraFieldSelectProps {
    disabled?: boolean
    options?: any
    val?: any
}

export const ExtraFieldSelect = (field: WrappedFieldProps<{}> & ExtraFieldSelectProps) => {

    function handleChange(event) {
        field.val = event.target.value;
    }

    return (
        <div>
            <select
                {...field.input}
                value={field.val}
                onChange={handleChange}
                className="form-control"
                disabled={field.disabled === true}>
                {field.options.map((opt: string, i: number) => {
                    return <option key={i} value={opt}>{opt}</option>;
                })}
            </select>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};

export const ExtraFieldSelectInput = (props: ExtraFieldSelectProps) => {

    function handleChange(event) {
        props.val = event.target.value;
    }

    return (
        <div>
            <select
                value={props.val===null?"":props.val}
                onChange={handleChange}
                className="form-control"
                disabled={props.disabled === true}>
                {props.options.map((opt: string, i: number) => {
                    return <option key={i} value={opt}>{opt}</option>;
                })}
            </select>
        </div>
    );
}