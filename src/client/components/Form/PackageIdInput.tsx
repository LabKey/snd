import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'

interface PackageIdInputProps {
    view?: PACKAGE_VIEW
}

export const PackageIdInput = (field: WrappedFieldProps<{}> & PackageIdInputProps) => {
    const { input, view } = field;
    const value = view === PACKAGE_VIEW.CLONE ? '' : input.value;

    return (
        <div className="input-row">
            <input
                {...field.input}
                className="form-control"
                disabled={view === PACKAGE_VIEW.EDIT || view === PACKAGE_VIEW.VIEW}
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