import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

const OrderOptions: Array<{label: string, value: string}> = [
    {
        value: 'moveUp',
        label: 'Move Up'
    },
    {
        value: 'moveDown',
        label: 'Move Down'
    }
];

interface OrderSelectProps {
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLSelectElement>>
    required?: boolean
    value?: string
}

export const OrderSelect = (props: OrderSelectProps) => {
    return (
        <div className="input-row">
            <select
                className="form-control"
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.required === true}
                value={props.value}>
                <option />
                {OrderOptions.map((opt: {label: string, value: string}, i: number) => {
                    return <option key={i} value={opt.value}>{opt.label}</option>;
                })}
            </select>
        </div>
    );
};