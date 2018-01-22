
import * as React from 'react';
import { Button, Modal, Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { push } from 'react-router-redux'
import { Dispatch } from 'redux';
import * as actions from '../../Wizards/Projects/actions'

import { ProjectForm } from './ProjectForm'
import {AssignedPackageModel} from "../../SuperPackages/model";
import {PROJECT_WIZARD_TYPES} from "../../Wizards/Projects/constants";
import {ProjectWizardModel} from "../../Wizards/Projects/model";
import {queryPackageFullNarrative} from "../../Wizards/SuperPackages/actions";
import NarrativeRow from "../../SuperPackages/Forms/NarrativeRow";

export enum PROJECT_VIEW {
    REVISE,
    EDIT,
    NEW,
    VIEW
}

interface ProjectFormContainerOwnProps extends RouteComponentProps<{idRev: string}> {}

interface ProjectFormContainerState {
    dispatch?: Dispatch<any>

    idRev: string
    model?: ProjectWizardModel
    view?: string
}

interface ProjectFormContainerStateProps {}

type ProjectFormContainerProps = ProjectFormContainerOwnProps & ProjectFormContainerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: ProjectFormContainerOwnProps) {
    const { idRev } = ownProps.match.params;
    const { path } = ownProps.match;

    const parts = path.split('/');
    let view;
    if (parts && parts[2]) {
        view = parts[2];
    }

    let id = (idRev ? idRev : -1);

    return {
        idRev: id,
        model: state.wizards.projects.projectData[id],
        view
    };
}

export class ProjectFormContainerImpl extends React.Component<ProjectFormContainerProps, ProjectFormContainerStateProps> {

    private panelHeader: React.ReactNode = null;
    private view: PROJECT_VIEW;

    constructor(props?: ProjectFormContainerProps) {
        super(props);

        const { idRev, view } = props;

        switch (view) {
            case 'view':
                this.view = PROJECT_VIEW.VIEW;
                break;

            case 'edit':
                this.view = PROJECT_VIEW.EDIT;
                break;

            case 'revise':
                this.view = PROJECT_VIEW.REVISE;
                break;

            case 'new':
                this.view = PROJECT_VIEW.NEW;
                break;
        }


        this.panelHeader = resolveProjectHeader(this.view, idRev);

        this.closeFullNarrative = this.closeFullNarrative.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        // this.renderNarrative = this.renderNarrative.bind(this);
        this.setModelWarning = this.setModelWarning.bind(this);
        this.showFullNarrative = this.showFullNarrative.bind(this);
    }

    componentDidMount() {
        this.initModel(this.props);
    }

    // componentWillReceiveProps(nextProps?: ProjectFormContainerProps) {
    //     this.initModel(nextProps);
    // }

    componentWillUnmount() {
        const { dispatch, model } = this.props;
        if (model) {
            dispatch(model.invalidate());
        }
    }

    initModel(props: ProjectFormContainerProps) {
        const { dispatch, idRev, view } = props;
        dispatch(actions.init(idRev, PROJECT_VIEW[view.toUpperCase()]));
    }

    handleCancel() {
        const { dispatch } = this.props;
        dispatch(push('/projects'))
    }

    handleFieldChange(name, value) {
        const { dispatch, model } = this.props;

        dispatch(model.saveField(name, value));
    }

    handleSubmit(active: boolean) {
        const { dispatch, model } = this.props;
        dispatch(model.submitForm(active));
    }

    showFullNarrative(pkg: AssignedPackageModel, shouldQuery: boolean) {
        const { dispatch, model } = this.props;

        if (shouldQuery) {
            dispatch(queryPackageFullNarrative(pkg.PkgId, model, PROJECT_WIZARD_TYPES.PROJECT_FULL_NARRATIVE));
        }
        else {
            dispatch({
                type: PROJECT_WIZARD_TYPES.PROJECT_FULL_NARRATIVE,
                model,
                narrativePkg: pkg
            });
        }
    }

    closeFullNarrative() {
        const { dispatch, model } = this.props;

        dispatch({
            type: PROJECT_WIZARD_TYPES.PROJECT_CLOSE_FULL_NARRATIVE,
            model
        });
    }

    renderNarrative(narrativePkg: AssignedPackageModel, level: number) {
        const { SubPackages } = narrativePkg;
        const key = "narrative-row-" + (narrativePkg.SuperPkgId || narrativePkg.altId);

        return (
            <div key={key}>
                <NarrativeRow model={narrativePkg} level={level} />
                {SubPackages.map((subPackage) =>
                    this.renderNarrative(subPackage, level + 1)
                )}
            </div>
        );
    }

    renderBody() {
        const { model } = this.props;

        if (model && model.projectLoaded && !model.isError) {
            return <ProjectForm
                handleCancel={this.handleCancel}
                handleFieldChange={this.handleFieldChange}
                handleFormSubmit={this.handleSubmit}
                handleWarning={this.setModelWarning}
                handleFullNarrative={this.showFullNarrative}
                isValid={model.isValid && !model.isSubmitting}
                model={model.data}
                view={model.formView}/>;
        }
        else if (model && model.isError) {
            return <div className='alert alert-danger package-wizard-model__error'>{model.message ? model.message : ''}</div>;
        }

        return <div><i className="fa fa-spinner fa-spin fa-fw"/> Loading...</div>;
    }

    renderModal() {
        const { model } = this.props;

        if (model) {
            if (model.isWarning && model.message) {
                return (
                    <div className="static-modal">
                        <Modal onHide={() => this.setModelWarning()} show={model.isWarning}>
                            <Modal.Body>
                                {model.message}
                            </Modal.Body>
                            <Modal.Footer>
                                <div className="btn-group">
                                    <Button onClick={() => this.setModelWarning()}>Confirm</Button>
                                </div>
                            </Modal.Footer>
                        </Modal>
                    </div>
                )
            }
            if (model.isSubmitting) {
                return (
                    <div className="static-modal">
                        <Modal onHide={() => null} show={model.isSubmitting}>
                            <Modal.Body>
                                <i className="fa fa-spinner fa-spin fa-fw"/> Submitting Project
                            </Modal.Body>
                        </Modal>
                    </div>
                )
            }
            if (model.narrativePkg != null) {
                return (
                    <div className="static-modal">
                        <Modal onHide={this.closeFullNarrative} show={model.narrativePkg != null}>
                            <Modal.Header closeButton>
                                <Modal.Title>Full Narrative for Package {model.narrativePkg.PkgId}</Modal.Title>
                            </Modal.Header>
                            <Modal.Body>
                                {this.renderNarrative(model.narrativePkg, 0)}
                            </Modal.Body>
                            <Modal.Footer>
                                <Button onClick={this.closeFullNarrative}>Close</Button>
                            </Modal.Footer>
                        </Modal>
                    </div>
                )
            }
        }
    }

    setModelWarning(warning?: string) {
        const { dispatch, model } = this.props;
        // if no warning is provided, warning will be toggled off and message removed
        dispatch(model.setWarning(warning));
    }

    render() {
        return (
            <Panel header={this.panelHeader}>
                {this.renderModal()}
                {this.renderBody()}
            </Panel>
        )
    }
}

export const ProjectFormContainer = connect<any, any, ProjectFormContainerProps>(mapStateToProps)(ProjectFormContainerImpl);

function resolveProjectHeader(view: PROJECT_VIEW, idRev) {

    let text = '';
    let parts = idRev.split('|');
    let id = parts[0], rev = parts[1];
    switch (view) {
        case PROJECT_VIEW.VIEW:
            text = 'View: Project ' + id + ', Revision ' + rev;
            break;

        case PROJECT_VIEW.EDIT:
            text = 'Edit: Project ' + id + ', Revision ' + rev;
            break;

        case PROJECT_VIEW.REVISE:
            text = 'Revise Project - Revision of ' + id + ', Revision ' + rev;
            break;

        case PROJECT_VIEW.NEW:
            text = 'New Project';
            break;

        default:
    }

    return (
        <div className="header--border__bottom">
            <h4>{text}</h4>
        </div>
    );
}