import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom'
import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { PackageSearchInput, PackageSearchResults, PackageViewerInput } from './PackageSearch'
import { SND_PKG_QUERY, SND_PKG_SCHEMA } from '../constants'
import { PackagesModel } from '../model'
import { PackageRow } from '../../../components/Packages/PackageRow'
import { querySelectRows, resolveKey } from '../../../query/actions'
import { QueryModel } from '../../../query/model'

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
        const dataExists = (packagesData && packagesData.isLoaded);
        const modelExists = (packagesModel && packagesModel.isInit);

        if (dataExists && !modelExists) {
            dispatch(packagesModel.init(packagesData));
        }
    }

    render() {
        const { data, filteredActive, filteredDrafts, isInit, showDrafts } = this.props.packagesModel;

        return (
            <Panel>
                <div className="row" style={{padding: '20px 0'}}>
                    <PackageSearchInput inputRenderer={PackageViewerInput}/>

                    <div style={{borderBottom: '1px solid black', margin: '0 15px'}}/>

                    <div className="col-sm-12 package-viewer__results">
                        {showDrafts ?
                            <div className="package_viewer__results--drafts">
                                <h4>Drafts</h4>
                                <div className="package_viewer__results-container">
                                    <PackageSearchResults
                                        data={data}
                                        dataIds={filteredDrafts}
                                        isLoaded={isInit}
                                        rowRenderer={PackageRow}/>
                                </div>
                            </div>
                        : null}

                        <div className="package_viewer__results--active clearfix">
                            <h4>Active</h4>
                            <div className="package_viewer__results-container">
                                <PackageSearchResults
                                    data={data}
                                    dataIds={filteredActive}
                                    isLoaded={isInit}
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