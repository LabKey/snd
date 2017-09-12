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
}

interface PackageFormContainerStateProps {}

type PackageFormContainerProps = PackageFormContainerOwnProps & PackageFormContainerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormContainerOwnProps) {
    const { id } = ownProps.match.params;
    let pkgId;
    if (id) {
        pkgId = id;
    }
    else {
        pkgId = 'newPackage';
    }

    return {
        id: pkgId,
        model: state.wizards.packages.packageData[pkgId]
    };
}

export class PackageFormContainerImpl extends React.Component<PackageFormContainerProps, PackageFormContainerStateProps> {

    private panelHeader: React.ReactNode = null;
    private view: PACKAGE_VIEW;

    constructor(props?: PackageFormContainerProps) {
        super(props);

        const { id } = props;
        const { path } = props.match;

        const parts = path.split('/');
        if (parts && parts[2]) {
            const view = parts[2];

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
        }

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

    handleButtonAction(action: string) {
        const { dispatch, history, model } = this.props;
        switch (action) {
            case 'cancel':
                history.goBack();
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

    handleSubmit(values) {
        return actions.save(values);
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
                        handleFormSubmit={this.handleSubmit}
                        handleNarrativeChange={this.handleNarrativeChange}
                        model={model.data}
                        view={this.view}/>;
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