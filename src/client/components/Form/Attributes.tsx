import * as React from 'react';

import { CheckboxInput } from './Checkbox'
import { DataTypeSelect } from './DataTypeSelect'
import { LookupKeyInput } from './LookupKeyInput'
import { NumericInput } from './NumericInput'
import { TextInput } from './TextInput'
import {OrderSelect} from "./OrderSelect";

interface AttributeColumnProps {
    disabled?: boolean
    inputComponent?: any
    label?: string
    name: string
    required?: boolean
    width?: string
}

const ATTRIBUTE_COLUMNS: Array<AttributeColumnProps> = [
    {
        disabled: true,
        inputComponent: TextInput,
        label: 'Key',
        name: 'name',
        required: true,
        width: '7vw'
    },
    {
        inputComponent: LookupKeyInput,
        label: 'Lookup Key',
        name: 'lookupKey',
        required: false,
        width: '13vw'
    },
    {
        inputComponent: DataTypeSelect,
        label: 'Data Type',
        name: 'rangeURI',
        required: true,
        width: '8vw'
    },
    {
        inputComponent: TextInput,
        label: 'Label',
        name: 'label',
        required: false,
        width: '13vw'
    },
    {
        inputComponent: NumericInput,
        label: 'Min',
        name: 'min',
        required: false,
        width: '8vw'
    },
    {
        inputComponent: NumericInput,
        label: 'Max',
        name: 'max',
        required: false,
        width: '8vw'
    },
    {
        inputComponent: TextInput,
        label: 'Default',
        name: 'defaultValue',
        required: false,
        width: '13vw'
    },
    {
        inputComponent: OrderSelect,
        label: 'Order',
        name: 'sortOrder',
        required: false,
        width: '12vw'
    },
    {
        inputComponent: CheckboxInput,
        label: 'Req.',
        name: 'required',
        required: false,
        width: '5vw'
    },
    {
        inputComponent: TextInput,
        label: 'Redacted Text',
        name: 'redactedText',
        required: false,
        width: '13vw'
    }
];

const AttributesGridHeader = () => {
    return (
        <thead>
            <tr>
                {ATTRIBUTE_COLUMNS.map((col: AttributeColumnProps, i: number) => {
                    return (
                        <th key={i} style={{whiteSpace: 'nowrap', width: col.width}}>
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
        const { attributes, attributeLookups, readOnly } = this.props;
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
                                        attributeLookups,
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
    attributeLookups?: any
    handleFieldChange?: any
    narrative: any
    readOnly?: boolean
}

export class Attributes extends React.Component<AttributesGridProps, {}> {
    render() {
        const { attributes, attributeLookups, handleFieldChange, narrative, readOnly } = this.props;

        return (
            <div>
                <div className="table-responsive">
                    <table className='table table-striped table-bordered'>
                        <AttributesGridHeader/>
                        <AttributesGridBody
                            attributes={attributes}
                            attributeLookups={attributeLookups}
                            handleFieldChange={handleFieldChange}
                            narrative={narrative}
                            readOnly={readOnly}/>
                    </table>
                </div>
            </div>
        )
    }
}

