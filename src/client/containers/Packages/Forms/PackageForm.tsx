import * as React from 'react';
import { Button, ControlLabel } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'
import { Form, FormProps, Field, reduxForm, initialize } from 'redux-form';

import { PackageModel } from '../../Wizards/Packages/model'
import { PACKAGE_VIEW } from './PackageFormContainer'
import { PackageIdInput } from '../../../components/Form/PackageIdInput'
import { TextArea } from '../../../components/Form/TextArea'
import { TextInput } from '../../../components/Form/TextInput'
import { Attributes } from '../../../components/Form/Attributes'

const styles = require<any>('./PackageForm.css');

const buttons = [
    {
        action: 'cancel',
        label: 'Cancel',
        type: 'button'
    },
    {
        action: 'saveDraft',
        label: 'Save as Draft',
        type: 'submit'
    },
    {
        action: 'submitReview',
        label: 'Submit for Review',
        type: 'submit'
    },
    {
        action: 'submitFinal',
        label: 'Submit Final',
        type: 'submit'
    },
];

interface PackageFormOwnProps {
    handleNarrativeChange?: (val) => void
    handleFormSubmit?: any
    model?: PackageModel
    view?: PACKAGE_VIEW
}

interface PackageFormState extends FormProps<any, any, any> {
    dispatch?: Dispatch<any>
}

interface PackageFormStateProps {

}

type PackageFormProps = PackageFormOwnProps & PackageFormState;

function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormOwnProps): PackageFormState {

    return {
        initialValues: ownProps.model
    };
}

export class PackageFormImpl extends React.Component<PackageFormProps, PackageFormStateProps> {

    constructor(props?: PackageFormProps) {
        super(props);

        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.submit = this.submit.bind(this);
    }

    handleButtonAction(action: string) {
        const { dispatch, model } = this.props;
        switch (action) {
            case 'cancel':
                break;

            case 'saveDraft':
                break;

            case 'submitReview':
                break;

            case 'submitFinal':
                break;

            default:
                return false;
        }
    }

    handleNarrativeChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        const { handleNarrativeChange } = this.props;
        const value = event.currentTarget.value;

        if (value && handleNarrativeChange && typeof handleNarrativeChange === 'function') {
            handleNarrativeChange(value);
        }

    }

    renderAttributes() {
        const { model, view } = this.props;
        if (model) {
            const { attributes, narrative } = model;
            return <Attributes attributes={attributes} narrative={narrative} readOnly={view === PACKAGE_VIEW.VIEW}/>
        }

        return null;
    }

    renderButtons() {
        const { view } = this.props;

        if (view !== PACKAGE_VIEW.VIEW) {
            // Todo enable/disable depending on form state
            return (
                <div className="btn-group pull-right">
                    {buttons.map((button, i) => {
                        return (
                            <Button
                                key={i}
                                type={button.type}>
                                {button.label}
                            </Button>
                        );
                    })}
                </div>
            );
        }
    }

    submit(values) {
        const { handleFormSubmit } = this.props;
        handleFormSubmit(values);
    }

    render() {
        const { handleSubmit, view } = this.props;

        return (
            <div>
                <form onSubmit={handleSubmit(this.submit)}>
                    <div className="row clearfix">
                        <div className="col-sm-8">
                            <div className="row clearfix">
                                <div className="col-xs-2">
                                    <ControlLabel>Package Id</ControlLabel>
                                </div>
                                <div className="col-xs-10">
                                    <ControlLabel>Description</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-2">
                                    <Field
                                        component={PackageIdInput}
                                        view={view}
                                        name='pkgId'/>
                                </div>
                                <div className="col-xs-10">
                                    <Field
                                        component={TextInput}
                                        disabled={view === PACKAGE_VIEW.VIEW}
                                        name='description'/>
                                </div>
                            </div>
                            <div className={"row clearfix " + styles['margin-top']}>
                                <div className="col-xs-12">
                                    <ControlLabel>Narrative</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <Field
                                        component={TextArea}
                                        disabled={view === PACKAGE_VIEW.VIEW}
                                        name='narrative'
                                        onChange={this.handleNarrativeChange}
                                        required={true}
                                        rows={6}/>
                                </div>
                            </div>
                        </div>
                        <div className="col-sm-4">

                        </div>
                    </div>

                    <div className="row clearfix">
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            <strong>Attributes <i className="fa fa-refresh" style={{cursor: 'pointer'}}/></strong>
                        </div>
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            {this.renderAttributes()}
                        </div>
                        <div className="col-sm-12">
                            {this.renderButtons()}
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}

const WrappedPackageForm = reduxForm({
    enableReinitialize: true,
    keepDirtyOnReinitialize: true,
    form: 'packageForm'
})(PackageFormImpl);

export const PackageForm = connect<PackageFormStateProps, any, PackageFormOwnProps>(mapStateToProps)(WrappedPackageForm);