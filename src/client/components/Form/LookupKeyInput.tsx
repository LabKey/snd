import * as React from 'react';

// add error check type behavior looking for schema.query format
interface LookupKeyInputProps {
    attribute?: any
    attributeId?: any
    attributeLookups?: any
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLSelectElement>>
    required?: boolean
    value?: string
}

export const LookupKeyInput = (props: LookupKeyInputProps) => {
    let value = '';

    if (props.value) {
        value = props.value;
    }

    return (
        <div className="input-row">
        <select
            value={value}
            name={props.name}
            onChange={props.onChange}
            className="form-control"
            disabled={props.disabled === true}>
            <option value=""/>
            {props.attributeLookups.map((opt: any, i: number) => {
                return <option key={i} value={opt.value}>{opt.label}</option>;
            })}
        </select>
        </div>
    );
};