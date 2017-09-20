import * as React from 'react';
import { WrappedFieldProps } from 'redux-form';

import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'

interface PkgIdInputProps {
    view?: PACKAGE_VIEW
}

export const FieldPackageIdInput = (field: WrappedFieldProps<{}> & PkgIdInputProps) => {
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

interface PackageIdInputProps  {
    name?: string
    onChange?: any
    value?: number
    view?: PACKAGE_VIEW
}

export const PackageIdInput = (props: PackageIdInputProps ) => {
    const { onChange, value } = props;

    return (
        <div className="input-row">
            <input
                className="form-control"
                disabled
                min={0}
                onChange={onChange}
                required
                type="text"
                value={value > -1 ? value : ""}
            />
        </div>
    );
};