import * as React from 'react';
import { Button, ControlLabel, ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PackageModel } from '../../Wizards/Packages/model'
import { PACKAGE_VIEW } from './PackageFormContainer'
import { CheckboxInput } from '../../../components/Form/Checkbox'
import { PackageIdInput } from '../../../components/Form/PackageIdInput'
import { TextArea } from '../../../components/Form/TextArea'
import { TextInput } from '../../../components/Form/TextInput'
import { Attributes } from '../../../components/Form/Attributes'
import { ExtraFields} from '../../../components/Form/ExtraFields'
import { QuerySearch } from '../../../query/QuerySearch'
import { SubpackageViewer } from './SubpackageViewer';
import { SuperPackageViewer } from './SuperPackageViewer';
import { AssignedPackageModel } from '../model'
import { CAT_SQ, REQUIRED_COLUMNS, TOPLEVEL_SUPER_PKG_SQ } from '../constants'
import { CategoriesSelect } from '../../Wizards/Packages/CategoriesSelect'
import { querySubPackageDetails } from '../../Wizards/Packages/actions'

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
    handleAssignedPackageAdd?: (assignedPackage: AssignedPackageModel) => void
    handleAssignedPackageRemove?: (assignedPackage: AssignedPackageModel) => void
    handleFormSubmit?: any
    handleWarning?: (warning?: string) => void
    isValid?: boolean
    model?: PackageModel
    view?: PACKAGE_VIEW
}

interface PackageFormState {
    dispatch?: Dispatch<any>
}

interface PackageFormStateProps {
    selectedSubPackage?: AssignedPackageModel
}

type PackageFormProps = PackageFormOwnProps & PackageFormState;

export class PackageFormImpl extends React.Component<PackageFormProps, PackageFormStateProps> {

    constructor(props?: PackageFormProps) {
        super(props);

        this.state = {
            selectedSubPackage: undefined
        };

        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.handleAssignedPackageAdd = this.handleAssignedPackageAdd.bind(this);
        this.handleAssignedPackageRemove = this.handleAssignedPackageRemove.bind(this);
        this.handleAssignedPackageClick = this.handleAssignedPackageClick.bind(this);
        this.submit = this.submit.bind(this);
    }

    handleButtonAction(action: string) {
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

    handleFieldChange(event: React.ChangeEvent<any>) {
        const { handleFieldChange } = this.props;
        const name = event.currentTarget.name,
            value = event.currentTarget.type === 'checkbox' ?
                event.currentTarget.checked : event.currentTarget.value;

        handleFieldChange(name, value);
    }

    handleNarrativeChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        const { handleNarrativeChange } = this.props;
        const value = event.currentTarget.value;

        if (handleNarrativeChange && typeof handleNarrativeChange === 'function') {
            handleNarrativeChange(value);
        }
    }

    handleAssignedPackageAdd(assignedPackage: AssignedPackageModel) {
        const { dispatch, model, handleFieldChange, handleWarning } = this.props;
        const { PkgId, Description, Narrative, Repeatable, SuperPkgId } = assignedPackage;

        if (!Repeatable) {
            // if the added package cannot be repeated and is already in the assigned packages array, emit warning
            const canAdd = model.subPackages.every((pkg) => {
                return pkg.PkgId !== PkgId;
            });

            if (!canAdd) {
                return handleWarning(['Package', PkgId, 'is not repeatable.'].join(' '));
            }
        }

        // create a new AssignedPackageModel object as the SuperPkgId needs to be undefined as it will be set on save/submit
        let newAssignedPackage = new AssignedPackageModel(PkgId, Description, Narrative, Repeatable, SuperPkgId, model.subPackages.length);
        newAssignedPackage.loadingSubpackages = true;

        handleFieldChange('subPackages', model.subPackages.concat([newAssignedPackage]));
        this.setState({selectedSubPackage: newAssignedPackage});

        dispatch(querySubPackageDetails(PkgId, model.pkgId));
    }

    handleAssignedPackageRemove(assignedPackage: AssignedPackageModel) {
        const { model, handleFieldChange } = this.props;

        // if the remove is of a previously saved assigned package, we can remove it by SuperPkgId
        // otherwise use the generated altId to remove
        handleFieldChange('subPackages', model.subPackages.filter((subPackage) => {
            if (assignedPackage.SuperPkgId != undefined) {
                return subPackage.SuperPkgId != assignedPackage.SuperPkgId;
            }
            else {
                return subPackage.altId != assignedPackage.altId;
            }
        }));
    }

    handleAssignedPackageClick(assignedPackage: AssignedPackageModel) {
        const { selectedSubPackage } = this.state;
        let idProp = assignedPackage.SuperPkgId ? 'SuperPkgId' : 'altId';

        if (selectedSubPackage == undefined || selectedSubPackage[idProp] != assignedPackage[idProp]) {
            this.setState({selectedSubPackage: assignedPackage});
        }
        else {
            this.setState({selectedSubPackage: undefined});
        }
    }

    renderAttributes() {
        const { model, view } = this.props;
        if (model) {
            const { attributes, attributeLookups, hasEvent, hasProject, narrative } = model;
            const isReadOnly = view === PACKAGE_VIEW.VIEW ||
                (view === PACKAGE_VIEW.EDIT && (hasEvent || hasProject));
            return <Attributes
                attributes={attributes}
                attributeLookups={attributeLookups}
                handleFieldChange={this.handleFieldChange}
                narrative={narrative}
                readOnly={isReadOnly}/>
        }

        return null;
    }

    renderButtons() {
        const { isValid, view } = this.props;

        if (view !== PACKAGE_VIEW.VIEW) {
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

    renderCategories() {
        const { handleFieldChange, model, view } = this.props;
        const disabled = view === PACKAGE_VIEW.VIEW;

        return (
            <QuerySearch
                id='categoriesSelect'
                modelProps={{requiredColumns: REQUIRED_COLUMNS.CATS}}
                schemaQuery={CAT_SQ}>
                <CategoriesSelect
                    disabled={disabled}
                    handleChange={handleFieldChange}
                    values={model.categories}/>
            </QuerySearch>
        )
    }

    renderExtraFields() {
        const { model, view } = this.props;
        const disabled = view === PACKAGE_VIEW.VIEW;
        if (model) {
            const { extraFields } = model;
            return <ExtraFields
                extraFields={extraFields}
                disabled={disabled}
                handleFieldChange={this.handleFieldChange}
            />
        }

        return null;
    }

    renderSubpackages() {
        const { model, view } = this.props;
        const { selectedSubPackage } = this.state;
        const { hasEvent, hasProject } = model;
        const isReadyOnly = view === PACKAGE_VIEW.VIEW ||
            (view === PACKAGE_VIEW.EDIT && (hasEvent || hasProject));

        return (
            <div className="row clearfix">
                {!isReadyOnly ?
                    <div className="col-sm-6">
                        <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                            <ControlLabel>Available Packages</ControlLabel >
                        </div>
                        <div className="row col-xs-12">
                            <QuerySearch
                                id='superPackageViewer'
                                modelProps={{requiredColumns: REQUIRED_COLUMNS.TOP_LEVEL_SUPER_PKG}}
                                schemaQuery={TOPLEVEL_SUPER_PKG_SQ}>
                                <SuperPackageViewer
                                    schemaQuery={TOPLEVEL_SUPER_PKG_SQ}
                                    handleAssignedPackageAdd={this.handleAssignedPackageAdd}
                                    view={view}/>
                            </QuerySearch>
                        </div>
                    </div>
                    : null
                }
                <div className={isReadyOnly ? "col-sm-12" : "col-sm-6"}>
                    <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                        <ControlLabel>Assigned Packages</ControlLabel >
                        <SubpackageViewer
                            subPackages={model.subPackages}
                            selectedSubPackage={selectedSubPackage}
                            handleAssignedPackageRemove={this.handleAssignedPackageRemove}
                            handleRowClick={this.handleAssignedPackageClick}
                            view={view}/>
                    </div>
                    <div className={"row clearfix col-xs-12 " + styles['margin-top']}>
                        <ListGroupItem className="data-search__container" style={{height: '90px'}}>
                            <div className="data-search__row">
                                {selectedSubPackage != undefined
                                    ? selectedSubPackage.Narrative
                                    : <div className={styles['narrative-none']}>
                                        Select a package to view its narrative.
                                      </div>
                                }
                            </div>
                        </ListGroupItem>
                    </div>
                </div>
            </div>
        );
    }

    submit(active: boolean = false) {
        const { handleFormSubmit } = this.props;
        event.preventDefault();

        handleFormSubmit(active);
    }

    render() {
        const { handleFieldChange, model, view } = this.props;
        const disabled = view === PACKAGE_VIEW.VIEW ||
            (view === PACKAGE_VIEW.EDIT && (model.hasEvent || model.hasProject));

        return (
            <div>
                <form>
                    <div className="row clearfix">
                        <div className="col-sm-8" style={{height: '220px'}}>
                            <div className="row clearfix">
                                <div className="col-xs-2">
                                    <ControlLabel htmlFor='pkgId'>Package Id</ControlLabel>
                                </div>
                                <div className="col-xs-10">
                                    <ControlLabel htmlFor='description'>Description *</ControlLabel>
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
                                        disabled={disabled}
                                        name='description'
                                        onChange={this.handleFieldChange}
                                        required
                                        value={model.description}/>
                                </div>
                            </div>
                            <div className={"row clearfix " + styles['margin-top']}>
                                <div className="col-xs-12">
                                    <ControlLabel htmlFor='narrative'>Narrative *</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <TextArea
                                        disabled={disabled}
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
                            {this.renderCategories()}
                        </div>
                    </div>

                    <div className="row clearfix">
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            <strong>Attributes</strong>
                        </div>
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            {this.renderAttributes()}
                        </div>
                        <div className={"col-sm-12 " + styles['margin-bottom']}>
                            <CheckboxInput
                                disabled={disabled}
                                name='repeatable'
                                onChange={this.handleFieldChange}
                                value={model.repeatable}/>
                            Allow multiple instances of this package in one super package
                        </div>
                    </div>
                    <div style={{borderBottom: '1px solid black'}}/>

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