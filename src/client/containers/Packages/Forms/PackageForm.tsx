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

interface ButtonListProps {
    action: string
    disabled?: boolean
    label: string
    type: string
}
const buttons: Array<ButtonListProps> = [
    {
        action: 'saveDraft',
        label: 'Save as Draft',
        type: 'submit'
    },
    {
        action: 'submitReview',
        disabled: true,
        label: 'Submit for Review',
        type: 'submit'
    },
    {
        action: 'save',
        label: 'Save',
        type: 'submit'
    },
];

interface PackageFormOwnProps {
    handleCancel?: () => void
    handleFieldChange?: (event: any) => void
    handleNarrativeChange?: (val) => void
    handleFormSubmit?: any
    isValid?: boolean
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

        this.handleCancel = this.handleCancel.bind(this);
        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.submit = this.submit.bind(this);
    }

    handleButtonAction(action: string) {
        const { dispatch, model } = this.props;
        switch (action) {
            case 'cancel':
                this.handleCancel();
                break;

            case 'saveDraft':
                this.submit(false);
                break;

            case 'submitReview':
                break;

            case 'save':
                this.submit(true);
                break;

            default:
                return false;
        }
    }

    handleCancel() {
        this.props.handleCancel();
    }

    handleNarrativeChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        const { handleNarrativeChange } = this.props;
        const value = event.currentTarget.value;

        if (value && handleNarrativeChange && typeof handleNarrativeChange === 'function') {
            handleNarrativeChange(value);
        }
    }

    renderAttributes() {
        const { handleFieldChange, model, view } = this.props;
        if (model) {
            const { attributes, narrative } = model;
            return <Attributes
                        attributes={attributes}
                        handleFieldChange={handleFieldChange}
                        narrative={narrative}
                        readOnly={view === PACKAGE_VIEW.VIEW}/>
        }

        return null;
    }

    renderButtons() {
        const { isValid, view } = this.props;

        if (view !== PACKAGE_VIEW.VIEW) {
            // Todo enable/disable depending on form state
            return (
                <div className="btn-group pull-right">
                    <Button
                        onClick={() => this.handleButtonAction('cancel')}>
                        Cancel
                    </Button>
                    {buttons.map((button: ButtonListProps, i: number) => {
                        return (
                            <Button
                                disabled={button.disabled === true || !isValid}
                                key={i}
                                onClick={() => this.handleButtonAction(button.action)}>
                                {button.label}
                            </Button>
                        );
                    })}
                </div>
            );
        }
    }

    submit(active: boolean = false) {
        const { handleFormSubmit } = this.props;
        event.preventDefault();

        handleFormSubmit(active);
    }

    render() {
        const { handleFieldChange, model, view } = this.props;

        return (
            <div>
                <form>
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
                                    <PackageIdInput
                                        name='pkgId'
                                        onChange={handleFieldChange}
                                        value={model.pkgId}
                                        view={view}/>
                                </div>
                                <div className="col-xs-10">
                                    <TextInput
                                        name='description'
                                        onChange={handleFieldChange}
                                        required
                                        value={model.description}/>
                                </div>
                            </div>
                            <div className={"row clearfix " + styles['margin-top']}>
                                <div className="col-xs-12">
                                    <ControlLabel>Narrative</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <TextArea
                                        disabled={view === PACKAGE_VIEW.VIEW}
                                        name='narrative'
                                        onChange={this.handleNarrativeChange}
                                        required={true}
                                        rows={6}
                                        value={model.narrative}/>
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

export const PackageForm = connect<PackageFormStateProps, any, PackageFormOwnProps>(mapStateToProps)(PackageFormImpl);