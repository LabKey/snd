import * as React from 'react';
import { Button, Modal, Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { push } from 'react-router-redux'
import { Dispatch } from 'redux';

import * as actions from '../../Wizards/Packages/actions'
import { PackageWizardModel } from '../../Wizards/Packages/model'

import { PackageForm } from './PackageForm'
import { AssignedPackageModel } from "../model";
import { PKG_WIZARD_TYPES } from '../../Wizards/Packages/constants'
import { queryPackageFullNarrative } from '../../Wizards/Packages/actions'
import NarrativeRow from './NarrativeRow'

const styles = require<any>('./PackageForm.css');

export enum PACKAGE_VIEW {
    CLONE,
    EDIT,
    NEW,
    VIEW
}

interface PackageFormContainerOwnProps extends RouteComponentProps<{id: string}> {}

interface PackageFormContainerState {
    dispatch?: Dispatch<any>

    id: string
    model?: PackageWizardModel
    view?: string
}

interface PackageFormContainerStateProps {}

type PackageFormContainerProps = PackageFormContainerOwnProps & PackageFormContainerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormContainerOwnProps) {
    const { id } = ownProps.match.params;
    const { path } = ownProps.match;

    const parts = path.split('/');
    let view;
    if (parts && parts[2]) {
        view = parts[2];
    }

    let pkgId;
    if (id) {
        pkgId = id;
    }
    else {
        pkgId = -1;
    }

    return {
        id: pkgId,
        model: state.wizards.packages.packageData[pkgId],
        view
    };
}

export class PackageFormContainerImpl extends React.Component<PackageFormContainerProps, PackageFormContainerStateProps> {

    private panelHeader: React.ReactNode = null;
    private view: PACKAGE_VIEW;

    constructor(props?: PackageFormContainerProps) {
        super(props);

        const { id, view } = props;

        switch (view) {
            case 'view':
                this.view = PACKAGE_VIEW.VIEW;
                break;

            case 'edit':
                this.view = PACKAGE_VIEW.EDIT;
                break;

            case 'clone':
                this.view = PACKAGE_VIEW.CLONE;
                break;

            case 'new':
                this.view = PACKAGE_VIEW.NEW;
                break;
        }


        this.panelHeader = resolvePackageHeader(this.view, id);

        this.closeFullNarrative = this.closeFullNarrative.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.renderNarrative = this.renderNarrative.bind(this);
        this.setModelWarning = this.setModelWarning.bind(this);
        this.showFullNarrative = this.showFullNarrative.bind(this);
    }

    componentDidMount() {
        this.initModel(this.props);
    }

    componentWillReceiveProps(nextProps?: PackageFormContainerProps) {
        this.initModel(nextProps);
    }

    initModel(props: PackageFormContainerProps) {
        const { dispatch, id, view } = props;
        dispatch(actions.init(id, PACKAGE_VIEW[view.toUpperCase()]));
    }

    handleCancel() {
        const { dispatch } = this.props;
        dispatch(push('/packages'))
    }

    handleFieldChange(name, value) {
        const { dispatch, model } = this.props;

        dispatch(model.saveField(name, value));
    }

    handleSubmit(active: boolean) {
        const { dispatch, model } = this.props;
        dispatch(model.submitForm(active));
    }

    handleNarrativeChange(value) {
        const { dispatch, model } = this.props;
        if (model) {
            dispatch(model.saveNarrative(value));
        }
    }

    showFullNarrative(pkg: AssignedPackageModel, shouldQuery: boolean) {
        const { dispatch, model } = this.props;

        if (shouldQuery) {
            dispatch(queryPackageFullNarrative(pkg.PkgId, model));
        }
        else {
            dispatch({
                type: PKG_WIZARD_TYPES.PACKAGE_FULL_NARRATIVE,
                model,
                narrativePkg: pkg
            });
        }
    }

    closeFullNarrative() {
        const { dispatch, model } = this.props;

        dispatch({
            type: PKG_WIZARD_TYPES.PACKAGE_CLOSE_FULL_NARRATIVE,
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

        if (model && model.packageLoaded) {
            return <PackageForm
                        handleCancel={this.handleCancel}
                        handleFieldChange={this.handleFieldChange}
                        handleFormSubmit={this.handleSubmit}
                        handleNarrativeChange={this.handleNarrativeChange}
                        handleFullNarrative={this.showFullNarrative}
                        handleWarning={this.setModelWarning}
                        isValid={model.isValid}
                        model={model.data}
                        view={model.formView}/>;
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
                                <i className="fa fa-spinner fa-spin fa-fw"/> Submitting Package
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

export const PackageFormContainer = connect<any, any, PackageFormContainerProps>(mapStateToProps)(PackageFormContainerImpl);

function resolvePackageHeader(view: PACKAGE_VIEW, id) {

    let text = '';
    switch (view) {
        case PACKAGE_VIEW.VIEW:
            text = 'View Package - ' + id;
            break;

        case PACKAGE_VIEW.EDIT:
            text = 'Edit Package - ' + id;
            break;

        case PACKAGE_VIEW.CLONE:
            text = 'New Package - Clone of ' + id;
            break;

        case PACKAGE_VIEW.NEW:
            text = 'New Package';
            break;

        default:
    }

    return (
        <div className={styles['header--border__bottom']}>
            <h4>{text}</h4>
        </div>
    );
}