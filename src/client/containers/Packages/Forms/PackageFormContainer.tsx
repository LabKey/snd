import * as React from 'react';
import { Button, Panel } from 'react-bootstrap'
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

const Buttons = [
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
        pkgId = 'newPackage';
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

        this.handleCancel = this.handleCancel.bind(this);
        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this.initModel(this.props);
    }

    componentWillReceiveProps(nextProps?: PackageFormContainerProps) {
        this.initModel(nextProps);
    }

    initModel(props: PackageFormContainerProps) {
        const { dispatch, id } = props;
        dispatch(actions.init(id, this.view));
    }

    handleCancel() {
        const { history } = this.props;
        history.goBack();
    }

    handleFieldChange(event) {
        const { dispatch, model } = this.props;
        const name = event.currentTarget.name,
            value = event.currentTarget.value;

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
                        isValid={model.isValid}
                        model={model.data}
                        view={model.formView}/>;
        }

        return <div>Loading...</div>;
    }

    render() {
        return (
            <Panel header={this.panelHeader}>
                {this.renderBody()}
            </Panel>
        )
    }
}

export const PackageFormContainer = connect<any, any, PackageFormContainerProps>(mapStateToProps)(PackageFormContainerImpl);

function resolvePackageHeader(view: PACKAGE_VIEW, id) {

    switch (view) {
        case PACKAGE_VIEW.VIEW:
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>View Package - {id}</h4>
                </div>
            );

        case PACKAGE_VIEW.EDIT:
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>Edit Package - {id}</h4>
                </div>
            );

        case PACKAGE_VIEW.CLONE:
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>New Package - Clone of {id}</h4>
                </div>
            );

        case PACKAGE_VIEW.NEW:
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>New Package</h4>
                </div>
            );

        default:
            return null;
    }
}