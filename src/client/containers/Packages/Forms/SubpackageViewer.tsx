import * as React from 'react';
import { ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PACKAGE_VIEW } from './PackageFormContainer'

interface SubpackageViewerOwnProps {
    packageIds: Array<number>
    view?: PACKAGE_VIEW
}

interface SubpackageViewerState {
    dispatch?: Dispatch<any>
}

type SubpackageViewerProps = SubpackageViewerOwnProps & SubpackageViewerState;

export class SubpackageViewerImpl extends React.Component<SubpackageViewerProps, {}> {

    render() {
        const { packageIds, view } = this.props;

        return (
            <ListGroupItem className="data-search__container" style={{height: '150px', overflowY: 'scroll'}}>
                <div className="data-search__row">
                    {/*TODO: current subpackages will go here*/}
                </div>
            </ListGroupItem>
        )
    }
}

export const SubpackageViewer = connect<any, any, SubpackageViewerProps>(null)(SubpackageViewerImpl);