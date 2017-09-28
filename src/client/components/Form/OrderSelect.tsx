import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

const OrderOptions: Array<{label: string, value: string}> = [
    {
        value: 'up',
        label: 'Move Up'
    },
    {
        value: 'down',
        label: 'Move Down'
    }
];

interface OrderSelectProps {
    disabled?: boolean
    first?: boolean
    last?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLSelectElement>>
    required?: boolean
    value?: string
}

export const OrderSelect = (props: OrderSelectProps) => {
    return (
        <div className="input-row">
            <div style={{float: 'left', width: '25%', padding: '5px 0 0'}}>
                {props.value}
            </div>
            <div style={{float: 'left', width: '75%'}}>
                <select
                    className="form-control"
                    disabled={props.disabled === true}
                    name={props.name}
                    onChange={props.onChange}
                    required={props.required === true}
                    value={props.value}>
                    <option />
                    {OrderOptions.filter((o) => {
                        return (!props.first && !props.last) ||
                            (props.first && o.value !== 'up') ||
                            (props.last&& o.value !== 'down');
                    }).map((opt: {label: string, value: string}, i: number) => {
                        return <option key={i} value={opt.value}>{opt.label}</option>;
                    })}
                </select>
            </div>
        </div>
    );
};