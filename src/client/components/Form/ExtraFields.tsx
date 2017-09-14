import * as React from 'react';
import {ControlLabel} from 'react-bootstrap'

import {PACKAGE_VIEW} from '../../containers/Packages/Forms/PackageFormContainer'
import {TextInput} from '../../components/Form/TextInput'
import {ExtraFieldSelectInput} from "./ExtraFieldSelect";

interface ExtraFieldsProps
{
    disabled?: any
    extraFields?: any
    handleFieldChange?: any
    name?: string
    view?: PACKAGE_VIEW
}

export class ExtraFields extends React.Component<ExtraFieldsProps, any>
{
    constructor(props)
    {
        super(props);

    }

    render()
    {
        const {extraFields, disabled, handleFieldChange} = this.props;
        let count = -1;

        if (extraFields && extraFields.length)
        {

            return (
                <div className="row clearfix">
                    {extraFields.map((extra) =>
                        {
                            let {lookupValues, value, name} = extra;
                            count++;
                            return (

                                <div key={"extraCol-" + name} className="row col-sm-6">
                                    <div className="col-xs-12">
                                        <ControlLabel>{name}</ControlLabel>
                                    </div>
                                    {lookupValues ? (
                                            <div className="col-xs-12">
                                                {React.createElement(ExtraFieldSelectInput,
                                                    {
                                                        disabled: disabled,
                                                        options: lookupValues,
                                                        handleFieldChange: handleFieldChange,
                                                        val: value,
                                                        name: name,
                                                        index: count
                                                    }
                                                )}
                                            </div>

                                        ) : (
                                            <div className="col-xs-12">
                                                {React.createElement(TextInput,
                                                    {
                                                        disabled: disabled,
                                                        value: value,
                                                        name: name
                                                    }
                                                )}
                                            </div>
                                        )}
                                </div>
                            );
                        }
                    )}
                </div >
            );
        }

        return <div className="row col-sm-12" style={{height: '53px'}}/>
    }
}
