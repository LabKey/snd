import * as React from 'react';
import { Panel } from 'react-bootstrap'
import { RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { APP_STATE_PROPS } from '../../../reducers/index'
import * as actions from '../../Wizards/Packages/actions'
import { PackageModel } from '../../Wizards/Packages/model'

const styles = require<any>('./PackageForm.css');

interface PackageFormOwnProps extends RouteComponentProps<{id: string}> {}

interface PackageFormState {
    dispatch?: Dispatch<any>

    packageModel?: PackageModel
}

interface PackageFormStateProps {

}

type PackageFormProps = PackageFormOwnProps & PackageFormState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormOwnProps) {
    const { id } = ownProps.match.params;

    return {
        packageModel: state.wizards.packages.packageData[id]
    };
}

export class PackageFormImpl extends React.Component<PackageFormProps, PackageFormStateProps> {

    private timer: number = 0;
    private panelHeader: React.ReactNode = null;
    private view: string = undefined;

    constructor(props?: PackageFormProps) {
        super(props);

        const { path } = props.match;
        const { id } = props.match.params;

        const parts = path.split('/');
        if (parts && parts[2]) {
            this.view = parts[2];
            this.panelHeader = parsePackageHeader(parts[2], id);
        }

        this.state = {
            showDrafts: false
        }
    }

    componentDidMount() {
        this.getPackage(this.props);
    }

    componentWillUnmount() {
        clearTimeout(this.timer);
    }

    componentWillReceiveProps(nextProps?: PackageFormProps) {
        this.getPackage(nextProps);
    }

    getPackage(props?: PackageFormProps) {
        const { dispatch, packageModel } = props;
        const { id } = props.match.params;

        if (id && !packageModel) {
            dispatch(actions.fetchPackage(id));
        }
    }

    render() {

        // todo: will be own components/parsers
        return (
            <Panel header={this.panelHeader}>
                <div className="row clearfix">
                    <div className="col-sm-8">
                        <div className="row clearfix">
                            <div className="col-xs-2">
                                <strong>Package Id</strong>
                            </div>
                            <div className="col-xs-10">
                                <strong>Description</strong>
                            </div>
                        </div>
                        <div className="row clearfix">
                            <div className="col-xs-2">
                                <input type="text" style={{width: '100%'}}/>
                            </div>
                            <div className="col-xs-10">
                                <input type="text" style={{width: '100%'}}/>
                            </div>
                        </div>
                        <div className="row clearfix">
                            <div className="col-xs-12">
                                <strong>Narrative</strong>
                            </div>
                        </div>
                        <div className="row clearfix">
                            <div className="col-xs-12">
                                <textarea style={{width: '100%'}} rows={5}/>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="row clearfix">
                    <div className={"col-sm-12 " + styles['margin-top']}>
                        <strong>Attributes <i className="fa fa-refresh"/></strong>
                    </div>
                </div>

            </Panel>
        )
    }
}

export const PackageForm = connect<any, any, PackageFormProps>(mapStateToProps)(PackageFormImpl);

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