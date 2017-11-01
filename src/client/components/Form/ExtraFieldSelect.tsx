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

interface ExtraFieldSelectProps {
    disabled?: boolean
    handleFieldChange?: any
    index?: any
    name?: string
    options?: any
    val?: any
}

export const ExtraFieldSelectInput = (props: ExtraFieldSelectProps) => {

    return (
        <div>
            <select
                value={props.val===null?"":props.val}
                name={'extraFields_' + props.index + '_' + props.name}
                onChange={props.handleFieldChange}
                className="form-control"
                disabled={props.disabled === true}>
                <option disabled style={{display: 'none'}}/>
                {props.options.map((opt: string, i: number) => {
                    return <option key={i} value={opt}>{opt}</option>;
                })}
            </select>
        </div>
    );
};