
import * as React from 'react';

interface DatePickerProps {
    disabled?: boolean
    name?: string
    onChange?: any
    required?: boolean
    value?: string
}

export const DatePicker = (props: DatePickerProps) => {
    const { value } = props;
    let date = new Date(value);
    let dateString = date.getFullYear() + '-'
        + (date.getMonth().toString().length === 1 ? '0' + date.getMonth() : date.getMonth()) + '-'
        + (date.getDate().toString().length === 1 ? '0' + date.getDate() : date.getDate());

    return (
        <div className="input-row">
            <input
                className="details-input"
                type="date"
                disabled={props.disabled === true}
                required={props.required === true}
                value={dateString}
                onChange={props.onChange}
                />
        </div>
    );
};