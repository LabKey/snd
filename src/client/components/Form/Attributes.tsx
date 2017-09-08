import * as React from 'react';

import { connect } from 'react-redux';
import { FormProps, Field, FieldArray, reduxForm, WrappedFieldArrayProps } from 'redux-form';

import { APP_STATE_PROPS } from '../../reducers/index'

import { CheckboxInput } from './Checkbox'
import { DataTypeSelect } from './DataTypeSelect'
import { LookupKeyInput } from './LookupKeyInput'
import { NumericInput } from './NumericInput'
import { TextInput } from './TextInput'


interface AttributesGridOwnProps {
    attributes: any
    narrative: any
}

interface AttributesGridState extends FormProps<any, any, any> {}

interface AttributesGridStateProps {

}

type AttributesGridProps = AttributesGridOwnProps & AttributesGridState;

function mapStateToProps(state: APP_STATE_PROPS, ownProps: AttributesGridOwnProps): AttributesGridState {

    return {
        initialValues: {
            attributes: ownProps.attributes
        }
    };
}

export class AttributesGridImpl extends React.Component<AttributesGridProps, AttributesGridStateProps> {

    constructor(props?: AttributesGridProps) {
        super(props);

    }

    render() {

        const { attributes, narrative } = this.props;

        return (
            <div>
                <form>
                    <div className="table-responsive">
                        <table className='table table-striped table-bordered'>
                            <AttributesGridHeader/>
                            <AttributesGridBody attributes={attributes} narrative={narrative}/>
                        </table>
                    </div>

                </form>
            </div>
        )
    }
}

const AttributesGridForm = reduxForm({
    enableReinitialize: true,
    form: 'attributesGrid'
})(AttributesGridImpl);

export const Attributes = connect<AttributesGridStateProps, any, AttributesGridOwnProps>(mapStateToProps)(AttributesGridForm);

interface AttributeColumnProps {
    disabled?: boolean
    label?: string
    name: string
    inputComponent?: any
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
        label: 'dataType',
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
        const { attributes } = this.props;
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
                                                disabled={col.disabled}
                                                name={`attributes[${i}][${col.label + i}]`}/>
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
