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

import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'
import { PROJECT_VIEW } from '../../containers/Projects/Forms/ProjectFormContainer'

interface IdInputProps {
    view?: PACKAGE_VIEW | PROJECT_VIEW
}

interface IdInputProps  {
    name?: string
    onChange?: any
    value?: number
    view?: PACKAGE_VIEW | PROJECT_VIEW
}

export const IdInput = (props: IdInputProps ) => {
    const { onChange, value, view } = props;
    // Check if the view is Clone or New and if so hide the pkgId value as this will be set by the server
    const idValue = view !== PACKAGE_VIEW.CLONE && view !== PACKAGE_VIEW.NEW && view !== PROJECT_VIEW.NEW as any ? value : '';

    return (
        <div className="input-row">
            <input
                className="form-control"
                disabled
                min={0}
                onChange={onChange}
                required
                type="text"
                value={idValue}
            />
        </div>
    );
};