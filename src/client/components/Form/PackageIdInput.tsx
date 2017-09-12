import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'

interface PackageIdInputProps {
    view?: PACKAGE_VIEW
}

export const PackageIdInput = (field: WrappedFieldProps<{}> & PackageIdInputProps) => {
    const { view } = field;

    let value;
    if (view === PACKAGE_VIEW.CLONE || view === PACKAGE_VIEW.NEW) {
        value = '';
    }

    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled
                required
                type="text"
                value={value}/>
            {field.meta.touched && field.meta.error ?
                <div className="error">
                    <span>{field.meta.error}</span>
                </div>
                : null}
        </div>
    );
};