import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

interface FieldDataTypeSelectProps {
    disabled?: boolean
    required?: boolean
}

export const FieldDataTypeSelect = (field: WrappedFieldProps<{}> & FieldDataTypeSelectProps) => {
    return (
        <div className="input-row">
            <select
                {...field.input}
                className="form-control"
                disabled={field.disabled === true}
                required={field.required === true}>
                <option value=""/>
                {DataTypeOptions.map((opt: {label: string, value: string}, i: number) => {
                    return <option key={i} value={opt.value}>{opt.label}</option>;
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

const DataTypeOptions: Array<{label: string, value: string}> = [
    {
        value: 'http://www.w3.org/2001/XMLSchema#string',
        label: 'String'
    },
    {
        value: 'int',
        label: 'Integer'
    },
    {
        value: 'double',
        label: 'Decimal'
    },
    {
        value: 'boolean',
        label: 'Boolean'
    },
    {
        value: 'date',
        label: 'Date'
    },
    {
        value: 'dateTime',
        label: 'Date Time'
    },
    {
        value: 'ParticipantId',
        label: 'Animal Id'
    }

];

interface DataTypeSelectProps {
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLSelectElement>>
    required?: boolean
    value?: string
}

export const DataTypeSelect = (props: DataTypeSelectProps) => {
    return (
        <div className="input-row">
            <select
                className="form-control"
                disabled={props.disabled === true}
                name={props.name}
                onChange={props.onChange}
                required={props.required === true}
                value={props.value}>
                {DataTypeOptions.map((opt: {label: string, value: string}, i: number) => {
                    return <option key={i} value={opt.value}>{opt.label}</option>;
                })}
            </select>
        </div>
    );
};