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
import { WrappedFieldProps } from 'redux-form';

interface FieldTextAreaProps {
    disabled?: boolean
    cols?: number
    required?: boolean
    rows?: number
}

export const FieldTextArea = (field: WrappedFieldProps<{}> & FieldTextAreaProps) => {
    return (
        <div className="input-row">
            <textarea
                {...field.input}
                className="form-control"
                cols={field.cols}
                disabled={field.disabled === true}
                onChange={(event) => field.input.onChange(event)}
                required={field.required === true}
                rows={field.rows}/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};

interface TextAreaProps {
    cols?: number
    disabled?: boolean
    name?: string
    onChange?: React.EventHandler<React.ChangeEvent<HTMLTextAreaElement>>
    required?: boolean
    rows?: number
    value?: string
}

export const TextArea = (props: TextAreaProps) => {
    return (
        <div className="input-row">
            <textarea
                className="form-control"
                cols={props.cols}
                disabled={props.disabled === true}
                onChange={(event) => props.onChange(event)}
                required={props.required === true}
                rows={props.rows}
                value={props.value ? props.value : ''}/>
        </div>
    );
};