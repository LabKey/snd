/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
