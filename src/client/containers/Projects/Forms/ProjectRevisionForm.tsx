
import * as React from 'react';
import { Button, ControlLabel } from 'react-bootstrap'
import { connect } from 'react-redux';

import {ProjectButtonListProps, ProjectFormButtons, ProjectFormProps} from "./ProjectForm";
import {IdInput} from "../../../components/Form/IdInput";
import {NumericInput} from "../../../components/Form/NumericInput";
import {DatePicker} from "../../../components/Form/DatePicker";
import {ExtraFields} from "../../../components/Form/ExtraFields";
import {TextArea} from "../../../components/Form/TextArea";
import {VIEW_TYPES} from "../../App/constants";
import {CheckboxInput} from "../../../components/Form/Checkbox";

export class ProjectRevisionFormImpl extends React.Component<ProjectFormProps> {

    private oldId: number = null;
    private oldRev: number = null;
    private oldRefId: number = null;
    private oldStartDate: string = null;
    private oldEndDate: string = null;
    private oldDescription: string = null;

    constructor(props?: ProjectFormProps) {
        super(props);

        this.state = {
            selectedSubPackage: undefined
        };

        this.oldId = props.model.projectId;
        this.oldRev = props.model.revisionNum;
        this.oldRefId = props.model.referenceId;
        this.oldStartDate = props.model.startDate;
        this.oldEndDate = props.model.endDate;
        this.oldDescription = props.model.description;

        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.submit = this.submit.bind(this);
    }

    componentWillMount() {
        this.props.handleRevisedValues(this.oldEndDate);
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

    renderButtons() {
        const { isValid, view } = this.props;

        if (view !== VIEW_TYPES.PROJECT_VIEW) {
            return (
                <div className="btn-group pull-right">
                    <Button
                        id='cancelButton'
                        onClick={() => this.handleButtonAction('cancel')}>
                        Cancel
                    </Button>
                    {ProjectFormButtons.map((button: ProjectButtonListProps, i: number) => {
                        return (
                            <Button
                                id={button.id}
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

    renderExtraFields(revisedFields: boolean) {
        const { model } = this.props;

        if (model) {
            const { extraFields } = model;
            return <ExtraFields
                extraFields={extraFields}
                revisedFields={revisedFields}
                handleFieldChange={this.handleFieldChange}
            />
        }

        return null;
    }

    submit(active: boolean = false) {
        const { handleFormSubmit } = this.props;
        handleFormSubmit(active);
    }

    render() {
        const { handleWarning, model, view } = this.props;

        return (
            <div>
                <form>
                    <div className="row clearfix">
                        <div className="col-sm-6" style={{height: '180px'}}>
                            <div className="row clearfix">
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='projectIdOld'>Project Id</ControlLabel>
                                </div>
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='revisionNumOld'>Revision Number</ControlLabel>
                                </div>
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='referenceIdOld'>Charge Id*</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-4">
                                    <IdInput
                                        name='projectIdOld'
                                        onChange={this.handleFieldChange}
                                        value={this.oldId}
                                        view={view}/>
                                </div>
                                <div className="col-xs-4">
                                    <IdInput
                                        name='revisionNumOld'
                                        onChange={this.handleFieldChange}
                                        value={this.oldRev}
                                        view={view}/>
                                </div>
                                <div className="col-xs-4">
                                    <NumericInput
                                        disabled={true}
                                        name='referenceIdOld'
                                        onChange={this.handleFieldChange}
                                        required
                                        value={this.oldRefId}/>
                                </div>
                            </div>
                            <div className="row clearfix margin-top">
                                <div className="col-xs-6">
                                    <ControlLabel htmlFor='startDateOld'>Start Date*</ControlLabel>
                                </div>
                                <div className="col-xs-6">
                                    <ControlLabel htmlFor='endDateOld'>End Date</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-6">
                                    <DatePicker
                                        name='startDateOld'
                                        onChange={this.handleFieldChange}
                                        value={this.oldStartDate}
                                        required
                                        disabled={true}/>
                                </div>
                                <div className="col-xs-6">
                                    <DatePicker
                                        name='endDateRevised'
                                        onChange={this.handleFieldChange}
                                        value={model.endDateRevised}
                                        required={false}
                                        disabled={false}/>
                                </div>
                            </div>
                            <div className="margin-top">
                                {this.renderExtraFields(true)}
                            </div>
                        </div>
                        <div className="col-sm-6">
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <ControlLabel htmlFor='descriptionOld'>Description*</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <TextArea
                                        disabled={true}
                                        name='descriptionOld'
                                        onChange={this.handleFieldChange}
                                        required
                                        rows={7}
                                        value={this.oldDescription}/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style={{borderBottom: '1px solid black', marginBottom: '20px'}}/>
                    <div className="row clearfix">
                        <div className="col-sm-6" style={{height: '180px'}}>
                            <div className="row clearfix">
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='projectId'>Project Id</ControlLabel>
                                </div>
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='revisionNum'>Revision Number</ControlLabel>
                                </div>
                                <div className="col-xs-4">
                                    <ControlLabel htmlFor='referenceId'>Charge Id*</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-4">
                                    <IdInput
                                        name='projectId'
                                        onChange={this.handleFieldChange}
                                        value={model.projectId}
                                        view={view}/>
                                </div>
                                <div className="col-xs-4">
                                    <IdInput
                                        name='revisionNum'
                                        onChange={this.handleFieldChange}
                                        value={model.revisionNum + 1}
                                        view={view}/>
                                </div>
                                <div className="col-xs-4">
                                    <NumericInput
                                        disabled={false}
                                        name='referenceId'
                                        onChange={this.handleFieldChange}
                                        required={true}
                                        value={model.referenceId}/>
                                </div>
                            </div>
                            <div className="row clearfix margin-top">
                                <div className="col-xs-6">
                                    <ControlLabel htmlFor='startDate'>Start Date*</ControlLabel>
                                </div>
                                <div className="col-xs-6">
                                    <ControlLabel htmlFor='endDate'>End Date</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-6">
                                    <DatePicker
                                        name='startDate'
                                        onChange={this.handleFieldChange}
                                        value={model.startDate}
                                        required={true}
                                        disabled={false}/>
                                </div>
                                <div className="col-xs-6">
                                    <DatePicker
                                        name='endDate'
                                        onChange={this.handleFieldChange}
                                        value={model.endDate}
                                        required={false}
                                        disabled={false}/>
                                </div>
                            </div>
                            <div className="margin-top">
                                {this.renderExtraFields(false)}
                            </div>
                        </div>
                        <div className="col-sm-6">
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <ControlLabel htmlFor='description'>Description*</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <TextArea
                                        disabled={false}
                                        name='description'
                                        onChange={this.handleFieldChange}
                                        required
                                        rows={7}
                                        value={model.description}/>
                                </div>
                            </div>
                            <div className="row clearfix margin-top">
                                <div className="col-xs-12">
                                    <CheckboxInput
                                        disabled={false}
                                        required={false}
                                        value={false}
                                        onChange={this.handleFieldChange}
                                        name='copyRevisedPkgs'
                                        />
                                    Copy assigned packages from revision {this.oldRev}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="row clearfix">
                        <div className="col-sm-12 margin-top">
                            {this.renderButtons()}
                        </div>
                    </div>
                </form>
            </div>
        )
    }

}

export const ProjectRevisionForm: any = connect()(ProjectRevisionFormImpl as any);