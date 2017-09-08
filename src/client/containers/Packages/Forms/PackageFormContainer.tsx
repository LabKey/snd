import * as React from 'react';
import { Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'


import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import * as actions from '../../Wizards/Packages/actions'
import { PackageWizardModel } from '../../Wizards/Packages/model'

import { ConnectedPackageForm } from './PackageForm'

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

    model?: PackageWizardModel
}

interface PackageFormContainerStateProps {}

type PackageFormContainerProps = PackageFormContainerOwnProps & PackageFormContainerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormContainerOwnProps) {
    const { id } = ownProps.match.params;

    return {
        model: state.wizards.packages.packageData[id]
    };
}

export class PackageFormContainerImpl extends React.Component<PackageFormContainerProps, PackageFormContainerStateProps> {

    private panelHeader: React.ReactNode = null;
    private view: PACKAGE_VIEW;

    constructor(props?: PackageFormContainerProps) {
        super(props);

        const { path } = props.match;
        const { id } = props.match.params;

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


            this.panelHeader = parsePackageHeader(this.view, id);
        }

        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
    }

    componentDidMount() {
        this.getPackage(this.props);
    }

    componentWillReceiveProps(nextProps?: PackageFormContainerProps) {
        this.getPackage(nextProps);
    }

    getPackage(props?: PackageFormContainerProps) {
        const { dispatch, model } = props;
        const { id } = props.match.params;

        if (id && !model) {
            dispatch(actions.fetchPackage(id));
        }
    }

    handleNarrativeChange(value) {
        const { dispatch, model } = this.props;
        if (model) {
            // this will break without model i.e. view === 'new'
            // need to add init model functionality and include getPackage into that function instead
            dispatch(model.saveNarrative(value));
        }
    }

    render() {
        const { model } = this.props;

        let body = null;

        if (model && model.packageLoaded && this.view !== PACKAGE_VIEW.NEW) {
            body = <ConnectedPackageForm handleNarrativeChange={this.handleNarrativeChange} model={model.data} view={this.view}/>;
            return (
                <Panel header={this.panelHeader}>
                    {body}
                </Panel>
            )
        }
        else if (this.view === PACKAGE_VIEW.NEW) {
            body = <ConnectedPackageForm handleNarrativeChange={this.handleNarrativeChange}/>;
        }

        else {
            body = <div>Loading...</div>;
        }

        return (
            <Panel header={this.panelHeader}>
                {body}
            </Panel>
        )
    }
}

export const PackageFormContainer = connect<any, any, PackageFormContainerProps>(mapStateToProps)(PackageFormContainerImpl);

function parsePackageHeader(view: PACKAGE_VIEW, id) {

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