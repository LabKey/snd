import * as React from 'react';
import { Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'


import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { APP_STATE_PROPS } from '../../../reducers/index'
import * as actions from '../../Wizards/Packages/actions'
import { PackageModel, PackageWizardModel } from '../../Wizards/Packages/model'

import { ConnectedPackageForm } from './PackageForm'

const styles = require<any>('./PackageForm.css');

interface PackageFormContainerOwnProps extends RouteComponentProps<{id: string}> {}

interface PackageFormContainerState {
    dispatch?: Dispatch<any>

    model?: PackageWizardModel
}

interface PackageFormContainerStateProps {

}

type PackageFormContainerProps = PackageFormContainerOwnProps & PackageFormContainerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormContainerOwnProps) {
    const { id } = ownProps.match.params;

    return {
        model: state.wizards.packages.packageData[id]
    };
}

export class PackageFormContainerImpl extends React.Component<PackageFormContainerProps, PackageFormContainerStateProps> {

    private panelHeader: React.ReactNode = null;
    private view: string = undefined;

    constructor(props?: PackageFormContainerProps) {
        super(props);

        const { path } = props.match;
        const { id } = props.match.params;

        const parts = path.split('/');
        if (parts && parts[2]) {
            this.view = parts[2];
            this.panelHeader = parsePackageHeader(parts[2], id);
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
        dispatch(model.saveNarrative(value));
    }

    render() {
        const { model } = this.props;

        let body = null;

        console.log(this.props)
        if (model && model.packageLoaded && this.view !== 'new') {
            const modelData = this.view !== 'new' ? model.data : undefined;
            body = <ConnectedPackageForm handleNarrativeChange={this.handleNarrativeChange} model={modelData} readOnly={this.view === 'view'}/>;
            return (
                <Panel header={this.panelHeader}>
                    {body}
                </Panel>
            )
        }
        else if (this.view === 'new') {
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

function parsePackageHeader(pathName: string, id) {

    switch (pathName) {
        case 'view':
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>View Package - {id}</h4>
                </div>
            );

        case 'edit':
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>Edit Package - {id}</h4>
                </div>
            );

        case 'clone':
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>New Package - Clone of {id}</h4>
                </div>
            );

        case 'new':
            return (
                <div className={styles['header--border__bottom']}>
                    <h4>New Package</h4>
                </div>
            );

        default:
            return null;
    }
}