import * as React from 'react';
import { Button, DropdownButton, SplitButton, MenuItem, Panel } from 'react-bootstrap';

import { Link, RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { CSSProperties } from "react";


import { APP_STATE_PROPS } from '../../../reducers/index'
import * as actions from '../actions'
import { QueryPackageModel, PackagesModel } from '../model'
import { PackageRow } from '../../../components/Packages/PackageRow'

interface PackageViewerOwnProps extends RouteComponentProps<{}> {}

interface PackageViewerState {
    dispatch?: Dispatch<any>

    packagesModel?: PackagesModel
}

interface PackageViewerStateProps {
    showDrafts: boolean
}

type PackageViewerProps = PackageViewerOwnProps & PackageViewerState;



function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageViewerOwnProps) {

    return {
        packagesModel: state.packages
    };
}

export class PackageViewerImpl extends React.Component<PackageViewerProps, PackageViewerStateProps> {

    private timer: number = 0;

    constructor(props?: PackageViewerProps) {
        super(props);

        this.state = {
            showDrafts: false
        }
    }

    componentDidMount() {
        const { dispatch } = this.props;
        // investigate mapDispatchToProps
        dispatch(actions.getPackages());
    }

    componentWillUnmount() {
        clearTimeout(this.timer);
    }

    componentWillReceiveProps(nextProps?: PackageViewerProps) {
        const { dispatch } = nextProps;
        const { isLoaded, isLoading, data } = nextProps.packagesModel;
        if (!data && !isLoading && !isLoaded) {
            dispatch(actions.getPackages());
        }
    }


    toggleDrafts(evt: React.MouseEvent<any>) {
        const { showDrafts } = this.state;

        evt.preventDefault();

        this.setState({
            showDrafts: !showDrafts
        })
    }

    handleClear() {
        const { dispatch, packagesModel } = this.props;
        const { hasInput } = packagesModel

        dispatch(actions.filterPackages(''));
    }

    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const { dispatch } = this.props;
        const input = evt.currentTarget.value;

        clearTimeout(this.timer);

        this.timer = setTimeout(() => {
            this.timer = null;

            dispatch(actions.filterPackages(input))
        }, 50);

    }

    render() {

        // todo: add css style sheet and webpack support
        const buttonStyle = {
            padding: '2.5px 15px',
        };

        const searchStyle = {
            borderRadius: '4px',
            padding: '5px 5px 5px 25px',
            fontSize: '1.1em',
            width: '100%'
        };

        const iconStyle: CSSProperties = {
            position: 'absolute',
            left: '21px',
            fontSize: '1.1em',
            top: '8px'
        };

        const clearStyle: CSSProperties = {
            position: 'absolute',
            cursor: 'pointer',
            right: '21px',
            fontSize: '1.1em',
            top: '8px',
            color: 'lightgray'
        };

        const { showDrafts } = this.state;
        const { data, filteredActive, filteredDrafts,  isLoaded } = this.props.packagesModel;

        // input should be own component
        return (
            <Panel>
                <div className="row" style={{padding: '20px 0 0'}}>
                    <div className="package-viewer__header clearfix" style={{paddingBottom: '20px'}}>
                        <div className="col-sm-3 col-md-2" style={buttonStyle}>
                            <Link to="/newPackage">
                                <Button>New Package</Button>
                            </Link>
                        </div>
                        <div className="col-sm-6 col-md-8" style={{position: 'relative'}}>
                            <i className="fa fa-search" style={iconStyle}/>
                            <i className="fa fa-times-circle" style={clearStyle} onClick={() => this.handleClear()}/>
                            <input type="text" name="packageSearch" style={searchStyle} onChange={(evt) => this.handleInputChange(evt)}/>
                        </div>
                        <div className="col-sm-3 col-md-2" style={buttonStyle}>
                            <div className="pull-right">
                                <DropdownButton id="package-actions" title="Options">
                                    <MenuItem>Edit Categories</MenuItem>
                                    <MenuItem>Edit Projects</MenuItem>
                                </DropdownButton>
                            </div>
                        </div>
                    </div>
                    <div style={{borderBottom: '1px solid black', margin: '0 15px'}}/>
                </div>
                {isLoaded && data ?
                    <div className="row" style={{padding: '20px 0'}}>
                        <div className="col-xs-12" style={{marginBottom: '10px', cursor: 'pointer'}} onClick={(evt) => this.toggleDrafts(evt)}>
                            <input type="checkbox" checked={showDrafts} onClick={(evt) => this.toggleDrafts(evt)}/> Show drafts
                        </div>
                        <div className="col-sm-12 package-viewer__results">
                            {showDrafts ?
                                <div className="package_viewer__results--drafts">
                                    <h4>Drafts</h4>
                                    <div className="package_viewer__results-container">
                                        {filteredDrafts.map((id) => {
                                            const pkg: QueryPackageModel = data[id];
                                            return <PackageRow key={"package_viewer__result" + id} pkg={pkg}/>;
                                        })}
                                    </div>
                                </div>
                                : null}

                            <div className="package_viewer__results--active clearfix">
                                <h4>Active</h4>
                                <div className="package_viewer__results-container">
                                    {filteredActive.map((id) => {
                                        const pkg: QueryPackageModel = data[id];
                                        return <PackageRow key={"package_viewer__result" + id} pkg={pkg}/>;
                                    })}
                                </div>
                            </div>
                        </div>
                    </div>

                : <div style={{padding: '20px 0 0'}}>Loading...</div>}
            </Panel>
        )
    }
}

export const PackageViewer = connect<any, any, PackageViewerProps>(mapStateToProps)(PackageViewerImpl);