import * as React from 'react';
import { Button, DropdownButton, SplitButton, MenuItem, Panel } from 'react-bootstrap';

import { Link, RouteComponentProps } from 'react-router-dom'

import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { CSSProperties } from "react";


import { APP_STATE_PROPS } from '../../../reducers/index'
import * as actions from '../actions'
import { QueryPackageModel, PackagesModel } from '../model'
import { SND_PKG_QUERY, SND_PKG_SCHEMA } from '../constants'
import { PackageRow } from '../../../components/Packages/PackageRow'
import { querySelectRows, resolveKey } from '../../../query/actions'
import { QueryModel } from '../../../query/model'

import { PackageSearchInput, PackageSearchResults } from './PackageSearch'

interface PackageViewerOwnProps extends RouteComponentProps<{}> {}

interface PackageViewerState {
    dispatch?: Dispatch<any>

    packagesData?: QueryModel
    packagesModel?: PackagesModel
}


type PackageViewerProps = PackageViewerOwnProps & PackageViewerState;

const resolvedSNDKey = resolveKey(SND_PKG_SCHEMA, SND_PKG_QUERY);

function mapStateToProps(state: APP_STATE_PROPS) {

    return {
        packagesData: state.queries.data[resolvedSNDKey],
        packagesModel: state.packages
    };
}

export class PackageViewerImpl extends React.Component<PackageViewerProps, {}> {

    constructor(props?: PackageViewerProps) {
        super(props);
    }

    componentDidMount() {
        const { dispatch, packagesModel } = this.props;
        if (!packagesModel || (packagesModel && !packagesModel.isInit)) {
            dispatch(querySelectRows(SND_PKG_SCHEMA, SND_PKG_QUERY));
        }
        // investigate mapDispatchToProps
    }

    componentWillReceiveProps(nextProps?: PackageViewerProps) {
        const { dispatch, packagesData, packagesModel } = nextProps;
        const pDataExists = (packagesData && packagesData.isLoaded);
        const pModelExists = (packagesModel && packagesModel.isInit);

        if (pDataExists && !pModelExists) {
            dispatch(packagesModel.init(packagesData));
        }
    }

    render() {
        const { data, filteredActive, filteredDrafts } = this.props.packagesModel;
        // todo: add css style sheet and webpack support

        return (
            <Panel>
                <div className="row" style={{padding: '20px 0'}}>
                    <PackageSearchInput inputRenderer={PackageViewerInput}/>

                    <div style={{borderBottom: '1px solid black', margin: '0 15px'}}/>

                    <div className="col-sm-12 package-viewer__results">
                        <div className="package_viewer__results--drafts">
                            <h4>Drafts</h4>
                            <div className="package_viewer__results-container">
                                <PackageSearchResults
                                    data={data}
                                    dataIds={filteredDrafts}
                                    rowRenderer={PackageRow}/>
                            </div>
                        </div>

                        <div className="package_viewer__results--active clearfix">
                            <h4>Active</h4>
                            <div className="package_viewer__results-container">
                                <PackageSearchResults
                                    data={data}
                                    dataIds={filteredActive}
                                    rowRenderer={PackageRow}/>
                            </div>
                        </div>
                    </div>
                </div>
            </Panel>
        )
    }
}

export const PackageViewer = connect<any, any, PackageViewerProps>(mapStateToProps)(PackageViewerImpl);


interface PackageViewerInputProps {
    dispatch?: Dispatch<{}>
}

interface PackageViewerInputStateProps {
    input?: string
    showDrafts?: boolean
}

export class PackageViewerInputImpl extends React.Component<PackageViewerInputProps, PackageViewerInputStateProps> {

    private timer: number = 0;
    private inputRef: HTMLInputElement;

    constructor(props?: any) {
        super(props);

        this.state = {
            input: '',
            showDrafts: false
        }
    }

    handleClear() {
        const { dispatch } = this.props;

        this.setInput('');
        this.inputRef.focus();
        dispatch(actions.filterPackages(''));
    }

    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const { dispatch } = this.props;
        const input = evt.currentTarget.value;

        this.setInput(input);

        clearTimeout(this.timer);

        this.timer = setTimeout(() => {
            this.timer = null;

            dispatch(actions.filterPackages(input))
        }, 50);
    }

    setInput(input: string) {
        this.setState({input});
    }

    toggleDrafts(evt: React.MouseEvent<any>) {
        const { showDrafts } = this.state;

        evt.preventDefault();

        this.setState({
            showDrafts: !showDrafts
        });
    }

    render() {
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

        const { input, showDrafts } = this.state;

        return(
            <div className="package-viewer__header clearfix" style={{paddingBottom: '20px'}}>
                <div className="col-sm-3 col-md-2" style={buttonStyle}>
                    <Link to="/packages/new">
                        <Button>New Package</Button>
                    </Link>
                </div>
                <div className="col-sm-6 col-md-8" style={{position: 'relative'}}>
                    <i className="fa fa-search" style={iconStyle}/>
                    <i className="fa fa-times-circle" style={clearStyle} onClick={() => this.handleClear()}/>
                    <input
                        name="packageSearch"
                        onChange={(evt) => this.handleInputChange(evt)}
                        ref={(el) => this.inputRef = el}
                        style={searchStyle}
                        type="text"
                        value={input}/>
                </div>
                <div className="col-sm-3 col-md-2" style={buttonStyle}>
                    <div className="pull-right">
                        <DropdownButton id="package-actions" title="Options">
                            <MenuItem>Edit Categories</MenuItem>
                            <MenuItem>Edit Projects</MenuItem>
                        </DropdownButton>
                    </div>
                </div>
                <div className="col-xs-12" style={{margin: '25px 0 10px', cursor: 'pointer'}} onClick={(evt) => this.toggleDrafts(evt)}>
                    <input type="checkbox" checked={showDrafts} onClick={(evt) => this.toggleDrafts(evt)}/> Show drafts
                </div>
            </div>
        )
    }
}

const PackageViewerInputWrap = (props: PackageViewerInputProps & Dispatch<{}>) => <PackageViewerInputImpl {...props}/>;
export const PackageViewerInput = connect()(PackageViewerInputWrap);