
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
    let dateString = "", year, month, day;

    if (value != null) {
        let dateParts, date = value.split(' ')[0];
        if (date.indexOf('-') !== -1) {
            dateParts = date.split('-');
        } else {
            dateParts = date.split('/');
        }
        year = dateParts[0];
        month = dateParts[1];
        day = dateParts[2];
        dateString = year + '-'
            + (month.toString().length === 1 ? '0' + month : month) + '-'
            + (day.toString().length === 1 ? '0' + day : day);
    }

    return (
        <div className="input-row">
            <input
                className="details-input"
                type="date"
                disabled={props.disabled === true}
                required={props.required === true}
                value={dateString}
                onChange={props.onChange}
                name={props.name}
                />
        </div>
    );
};