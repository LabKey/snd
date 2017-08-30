import * as React from 'react';

import { Link, RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { APP_STATE_PROPS } from '../../../reducers/index'
import * as actions from '../actions'
import { PackageModel } from '../model'


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
        packageModel: state.packages.packageData[id]
    };
}

export class PackageFormImpl extends React.Component<PackageFormProps, PackageFormStateProps> {

    private timer: number = 0;

    constructor(props?: PackageFormProps) {
        super(props);

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
            dispatch(actions.getPackage(id));
        }
    }

    render() {

        console.log(this.props.packageModel)
        return (
            <div>

            </div>
        )
    }
}

export const PackageForm = connect<any, any, PackageFormProps>(mapStateToProps)(PackageFormImpl);