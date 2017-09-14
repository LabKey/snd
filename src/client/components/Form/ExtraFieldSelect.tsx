import * as React from 'react';

interface ExtraFieldSelectProps {
    disabled?: boolean
    handleFieldChange?: any
    index?: any
    name?: string
    options?: any
    val?: any
}

export const ExtraFieldSelectInput = (props: ExtraFieldSelectProps) => {

    return (
        <div>
            <select
                value={props.val===null?"":props.val}
                name={'extraFields_' + props.index + '_' + props.name}
                onChange={props.handleFieldChange}
                className="form-control"
                disabled={props.disabled === true}>
                <option disabled style={{display: 'none'}}/>
                {props.options.map((opt: string, i: number) => {
                    return <option key={i} value={opt}>{opt}</option>;
                })}
            </select>
        </div>
    );
};