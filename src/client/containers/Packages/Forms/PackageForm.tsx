import * as React from 'react';
import { Button, ControlLabel } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PackageModel } from '../../Wizards/Packages/model'
import { PACKAGE_VIEW } from './PackageFormContainer'
import { PackageIdInput } from '../../../components/Form/PackageIdInput'
import { TextArea } from '../../../components/Form/TextArea'
import { TextInput } from '../../../components/Form/TextInput'
import { Attributes } from '../../../components/Form/Attributes'

import { QuerySearch } from '../../../query/QuerySearchInput'
import { SchemaQuery } from '../../../query/model'
import { SND_CATEGORY_QUERY, SND_PKG_SCHEMA } from '../constants'

const CAT_SQ = SchemaQuery.create(SND_PKG_SCHEMA, SND_CATEGORY_QUERY);

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
    handleFieldChange?: (name: string, value: any) => void
    handleNarrativeChange?: (val) => void
    handleFormSubmit?: any
    isValid?: boolean
    model?: PackageModel
    view?: PACKAGE_VIEW
}

interface PackageFormState {
    dispatch?: Dispatch<any>
}

type PackageFormProps = PackageFormOwnProps & PackageFormState;

export class PackageFormImpl extends React.Component<PackageFormProps, {}> {

    constructor(props?: PackageFormProps) {
        super(props);

        this.handleCancel = this.handleCancel.bind(this);
        this.handleCategoriesChange = this.handleCategoriesChange.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
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

    handleCategoriesChange(categories) {
        console.log(categories)
    }

    handleFieldChange(event: React.ChangeEvent<any>) {
        const { handleFieldChange } = this.props;
        const name = event.currentTarget.name,
            value = event.currentTarget.value;

        handleFieldChange(name, value);
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
            return <Attributes
                        attributes={attributes}
                        handleFieldChange={this.handleFieldChange}
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
                        <div className="col-sm-8" style={{height: '220px'}}>
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
                                        onChange={this.handleFieldChange}
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
                        <div className="col-sm-4" style={{height: '220px'}}>
                            <QuerySearch
                                handleChange={handleFieldChange}
                                name='categories'
                                schemaQuery={CAT_SQ}/>
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

export const PackageForm: any = connect()(PackageFormImpl as any); // fix typing