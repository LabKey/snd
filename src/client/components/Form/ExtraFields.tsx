import * as React from 'react';
import {ControlLabel} from 'react-bootstrap'
import {Field} from 'redux-form';

import {PACKAGE_VIEW} from '../../containers/Packages/Forms/PackageFormContainer'
import {TextInput} from '../../components/Form/TextInput'
import {ExtraFieldSelect, ExtraFieldSelectInput} from "./ExtraFieldSelect";

interface ExtraFieldsProps
{
    disabled?: any
    extraFields?: any
    name?: any
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
        const {extraFields, disabled} = this.props;
        let count = 0;

        if (extraFields && extraFields.length)
        {

            return (
                <div className="row clearfix">
                    {extraFields.map((extra) =>
                        {
                            {/*TODO: Will be removing concatenated zeros soon*/}
                            let {lookupValues0 : lookupValues, value0 : value, name0 : name} = extra;
                            count++;
                            return (

                                <div key={"extraCol-" + name} className="row col-sm-6">
                                    <div className="col-xs-12">
                                        <ControlLabel>{name}</ControlLabel>
                                    </div>
                                    {lookupValues ? (
                                            <div className="col-xs-12">
                                                {React.createElement(ExtraFieldSelectInput,
                                                    {disabled:disabled,
                                                    options:lookupValues,
                                                    val:value}
                                                )}
                                            </div>

                                        ) : (
                                            <div className="col-xs-12">
                                                {React.createElement(TextInput,
                                                    {disabled:disabled,
                                                    value:value}
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
