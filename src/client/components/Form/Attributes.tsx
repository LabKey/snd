import * as React from 'react';

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
    required?: boolean
}

const ATTRIBUTE_COLUMNS: Array<AttributeColumnProps> = [
    {
        disabled: true,
        inputComponent: TextInput,
        label: 'Attribute Key',
        name: 'name',
        required: true
    },
    {
        inputComponent: LookupKeyInput,
        label: 'Lookup Key',
        name: 'lookupKey',
        required: false
    },
    {
        inputComponent: DataTypeSelect,
        label: 'Data Type',
        name: 'rangeURI',
        required: true
    },
    {
        inputComponent: TextInput,
        label: 'Label',
        name: 'label',
        required: false
    },
    {
        inputComponent: NumericInput,
        label: 'Min',
        name: 'min',
        required: false
    },
    {
        inputComponent: NumericInput,
        label: 'Max',
        name: 'max',
        required: false
    },
    {
        inputComponent: TextInput,
        label: 'Default',
        name: 'default',
        required: false
    },
    {
        inputComponent: TextInput,
        label: 'Order',
        name: 'order',
        required: false
    },
    {
        inputComponent: CheckboxInput,
        label: 'Required',
        name: 'required',
        required: false
    },
    {
        inputComponent: TextInput,
        label: 'Redacted Text',
        name: 'redactedText',
        required: false
    }
];

const AttributesGridHeader = () => {
    return (
        <thead>
            <tr>
                {ATTRIBUTE_COLUMNS.map((col: AttributeColumnProps, i: number) => {
                    return (
                        <th key={i} style={{whiteSpace: 'nowrap'}}>
                            <strong>{col.label}{col.required ? ' *' : ''}</strong>
                        </th>
                    )
                })}
            </tr>
        </thead>
    )
};

class AttributesGridBody extends React.Component<AttributesGridProps, any> {
    constructor(props) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(event: React.ChangeEvent<any>) {
        const { handleFieldChange } = this.props;
        handleFieldChange(event);
    }

    render() {
        const { attributes, handleFieldChange, readOnly } = this.props;
        if (attributes && attributes.length) {
            return (
                <tbody>
                    {attributes.map((attribute, i) => {
                        return (
                            <tr key={i}>
                                {ATTRIBUTE_COLUMNS.map((col: AttributeColumnProps, j: number) => {
                                    const props = {
                                        attribute,
                                        attributeId: i,
                                        disabled: col.disabled || readOnly,
                                        name: `attributes_${i}_${col.name}`,
                                        onChange: this.handleChange,
                                        value: attribute[col.name],
                                        required: col.required
                                    };
                                    return (
                                        <td key={j}>
                                            {React.createElement(col.inputComponent, props)}
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

interface AttributesGridProps {
    attributes: any
    handleFieldChange?: any
    narrative: any
    readOnly?: boolean
}

export class Attributes extends React.Component<AttributesGridProps, {}> {
    render() {
        const { attributes, handleFieldChange, narrative, readOnly } = this.props;

        return (
            <div>
                <div className="table-responsive">
                    <table className='table table-striped table-bordered'>
                        <AttributesGridHeader/>
                        <AttributesGridBody
                            attributes={attributes}
                            handleFieldChange={handleFieldChange}
                            narrative={narrative}
                            readOnly={readOnly}/>
                    </table>
                </div>
            </div>
        )
    }
}

