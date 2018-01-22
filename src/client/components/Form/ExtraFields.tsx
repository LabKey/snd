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
        let colClass = "col-sm-6";

        // Auto generated fields limited to 3 right now
        if (extraFields.length > 3)
            extraFields.splice(3);

        if (extraFields.length === 3)
            colClass = "col-sm-4";

        if (extraFields && extraFields.length)
        {

            return (
                <div>
                    <div className="row clearfix">
                        {extraFields.map((extra) =>
                            {
                                let {name} = extra;
                                count++;
                                return (
                                    <div key={"extraCol-" + name} className={colClass}>
                                            <ControlLabel>{name}</ControlLabel>
                                    </div>
                                );
                            }
                        )}
                    </div>
                    <div className="row clearfix">
                        {extraFields.map((extra) =>
                            {
                                let {lookupValues, value, name} = extra;
                                count++;
                                return (

                                    <div key={"extraCol-" + name} className={colClass}>
                                        {lookupValues ? (
                                            <div>
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
                                                <div>
                                                    {React.createElement(TextInput,
                                                        {
                                                            disabled: disabled,
                                                            value: value,
                                                            onChange: handleFieldChange,
                                                            name: name
                                                        }
                                                    )}
                                                </div>
                                            )}
                                    </div>
                                );
                            }
                        )}
                    </div>
                </div>
            );
        }

        return <div className="row col-sm-12" style={{height: '53px'}}/>
    }
}
