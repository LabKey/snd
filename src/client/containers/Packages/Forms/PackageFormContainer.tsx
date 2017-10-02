import * as React from 'react';
import { Button, Modal, Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import * as actions from '../../Wizards/Packages/actions'
import { PackageWizardModel } from '../../Wizards/Packages/model'

import { PackageForm } from './PackageForm'

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

        this.dismissWarning = this.dismissWarning.bind(this);
        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.setModelWarning = this.setModelWarning.bind(this);
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
        const { history } = this.props;
        history.goBack();
    }

    handleFieldChange(name, value) {
        const { dispatch, model } = this.props;

        dispatch(model.saveField(name, value));
    }

    handleSubmit(active: boolean) {
        const { dispatch, history, model } = this.props;
        dispatch(model.submitForm(active, history.push));
    }

    handleNarrativeChange(value) {
        const { dispatch, model } = this.props;
        if (model) {
            dispatch(model.saveNarrative(value));
        }
    }

    renderBody() {
        const { model } = this.props;

        if (model && model.packageLoaded) {
            return <PackageForm
                        handleCancel={this.handleCancel}
                        handleFieldChange={this.handleFieldChange}
                        handleFormSubmit={this.handleSubmit}
                        handleNarrativeChange={this.handleNarrativeChange}
                        handleWarning={this.setModelWarning}
                        isValid={model.isValid}
                        model={model.data}
                        view={model.formView}/>;
        }

        return <div><i className="fa fa-spinner fa-spin fa-fw"/> Loading...</div>;
    }

    dismissWarning() {
        const { dispatch, model } = this.props;
        dispatch(model.setWarning());
    }

    setModelWarning(warning?: string) {
        const { dispatch, model } = this.props;
        dispatch(model.setWarning(warning));
    }

    renderModal() {
        const { model } = this.props;

        if (model && model.isWarning && model.message) {
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

        if (model && model.isSubmitting) {
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