import * as React from 'react';

import { FormProps, Field } from 'redux-form';


import { CheckboxInput } from './Checkbox'
import { DataTypeSelect } from './DataTypeSelect'
import { LookupKeyInput } from './LookupKeyInput'
import { NumericInput } from './NumericInput'
import { TextInput } from './TextInput'

interface AttributeColumnProps {
    disabled?: boolean
    inputComponent?: any
    label?: string
    name: string
}

const ATTRIBUTE_COLUMNS: Array<AttributeColumnProps> = [
    {
        disabled: true,
        label: 'name',
        name: 'Attribute Key',
        inputComponent: TextInput
    },
    {
        label: 'lookupKey',
        name: 'Lookup Key',
        inputComponent: LookupKeyInput
    },
    {
        label: 'rangeURI',
        name: 'Data Type',
        inputComponent: DataTypeSelect
    },
    {
        label: 'label',
        name: 'Label',
        inputComponent: TextInput

    },
    {
        label: 'min',
        name: 'Min',
        inputComponent: NumericInput
    },
    {
        label: 'max',
        name: 'Max',
        inputComponent: NumericInput
    },
    {
        label: 'default',
        name: 'Default',
        inputComponent: TextInput
    },
    {
        label: 'order',
        name: 'Order',
        inputComponent: TextInput
    },
    {
        label: 'required',
        name: 'Required',
        inputComponent: CheckboxInput

    },
    {
        label: 'redactedText',
        name: 'Redacted Text',
        inputComponent: TextInput
    }
];

const AttributesGridHeader = () => {
    return (
        <thead>
            <tr>
                {ATTRIBUTE_COLUMNS.map((col: AttributeColumnProps, i: number) => {
                    return (
                        <th key={i} style={{whiteSpace: 'nowrap'}}>
                            <strong>{col.name}</strong>
                        </th>
                    )
                })}
            </tr>
        </thead>
    )
};

class AttributesGridBody extends React.Component<AttributesGridOwnProps, any> {
    constructor(props) {
        super(props);

    }

    render() {
        const { attributes, readOnly } = this.props;
        if (attributes && attributes.length) {
            return (
                <tbody>
                    {attributes.map((attribute, i) => {
                        return (
                            <tr key={i}>
                                {ATTRIBUTE_COLUMNS.map((col: AttributeColumnProps, j: number) => {
                                    return (
                                        <td key={j}>
                                            <Field
                                                attribute={attribute}
                                                attributeId={i}
                                                component={col.inputComponent}
                                                disabled={col.disabled || readOnly}
                                                name={`attributes[${i}][${[col.label, i].join('_')}]`}/>
                                        </td>
                                    )
                                })}
                            </tr>
                        )
                    })}
                </tbody>
            )
        }

        return <tbody/>;
    }
}

interface AttributesGridOwnProps {
    attributes: any
    narrative: any
    readOnly?: boolean
}

interface AttributesGridState extends FormProps<any, any, any> {}

interface AttributesGridStateProps {

}

type AttributesGridProps = AttributesGridOwnProps & AttributesGridState;

export class Attributes extends React.Component<AttributesGridProps, AttributesGridStateProps> {

    constructor(props?: AttributesGridProps) {
        super(props);

    }

    render() {

        const { attributes, narrative, readOnly } = this.props;

        return (
            <div>
                <div className="table-responsive">
                    <table className='table table-striped table-bordered'>
                        <AttributesGridHeader/>
                        <AttributesGridBody attributes={attributes} narrative={narrative} readOnly={readOnly}/>
                    </table>
                </div>
            </div>
        )
    }
}

