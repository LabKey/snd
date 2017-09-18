import * as React from 'react';
import { Button, ControlLabel, ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PackageModel } from '../../Wizards/Packages/model'
import { PACKAGE_VIEW } from './PackageFormContainer'
import { PackageIdInput } from '../../../components/Form/PackageIdInput'
import { TextArea } from '../../../components/Form/TextArea'
import { TextInput } from '../../../components/Form/TextInput'
import { Attributes } from '../../../components/Form/Attributes'
import { ExtraFields} from '../../../components/Form/ExtraFields'
import { QuerySearch } from '../../../query/QuerySearchInput'
import { SubpackageViewer } from './SubpackageViewer';
import { SuperPackageViewer } from './SuperPackageViewer';
import { superPkgSchemaQuery as SUPER_PKG_SQ, catSchemaQuery as CAT_SQ } from '../model'

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

    renderExtraFields() {
        const { model, view } = this.props;
        if (model) {
            const { extraFields } = model;
            return <ExtraFields
                extraFields={extraFields}
                disabled={view === PACKAGE_VIEW.VIEW}
                handleFieldChange={this.handleFieldChange}
            />
        }

        return null;
    }

    renderAttributes() {
        const { model, view } = this.props;
        if (model) {
            const { attributes, attributeLookups, narrative } = model;
            return <Attributes
                        attributes={attributes}
                        attributeLookups={attributeLookups}
                        handleFieldChange={this.handleFieldChange}
                        narrative={narrative}
                        readOnly={view === PACKAGE_VIEW.VIEW}/>
        }

        return null;
    }

    renderSubpackages() {
        const { model, view } = this.props;
        let isReadyOnly = view == PACKAGE_VIEW.VIEW;

        return (
            <div className="row clearfix">
                { !isReadyOnly ?
                    <div className="col-sm-6">
                        <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                            <ControlLabel>Available Packages</ControlLabel >
                        </div>
                        <div className="row col-xs-12">
                            <QuerySearch schemaQuery={SUPER_PKG_SQ}>
                                <SuperPackageViewer schemaQuery={SUPER_PKG_SQ}/>
                            </QuerySearch>
                        </div>
                    </div>
                    : ''
                }
                <div className={isReadyOnly ? "col-sm-12" : "col-sm-6"}>
                    <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                        <ControlLabel>Assigned Packages</ControlLabel >
                        <SubpackageViewer packageIds={model.subPackages} view={view}/>
                    </div>
                    <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                        <ListGroupItem className="data-search__container" style={{height: '90px'}}>
                            <div className="data-search__row">
                                {/*TODO: Narrative will go here.*/}
                            </div>
                        </ListGroupItem>
                    </div>
                </div>
            </div>
        );
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
                        <div className="col-sm-4">
                            <div className="row col-sm-12">
                                {this.renderExtraFields()}
                            </div>
                            <div className="row col-sm-12">
                                <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                                    <ControlLabel>Categories</ControlLabel >
                                </div>
                                <div className="row col-xs-12">
                                    <QuerySearch
                                        handleChange={handleFieldChange}
                                        name='categories'
                                        schemaQuery={CAT_SQ}
                                        value={model.categories}/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="row clearfix">
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            <strong>Attributes <i className="fa fa-refresh" style={{cursor: 'pointer'}}/></strong>
                        </div>
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            {this.renderAttributes()}
                        </div>
                    </div>

                    {this.renderSubpackages()}

                    <div className="row clearfix">
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            {this.renderButtons()}
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}

export const PackageForm: any = connect()(PackageFormImpl as any); // fix typing