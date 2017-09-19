import * as React from 'react';
import { ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PACKAGE_VIEW } from './PackageFormContainer'
import { SuperPackageRow } from '../../../components/SuperPackages/SuperPackageRow'
import { AssignedPackageModel } from "../model";

interface SubpackageViewerOwnProps {
    subPackages: Array<AssignedPackageModel>
    handleAssignedPackageRemove: (assignedPackage: AssignedPackageModel) => any
    view?: PACKAGE_VIEW
}

interface SubpackageViewerState {
    dispatch?: Dispatch<any>
}

type SubpackageViewerProps = SubpackageViewerOwnProps & SubpackageViewerState;

export class SubpackageViewerImpl extends React.Component<SubpackageViewerProps, {}> {

    render() {
        const { subPackages, handleAssignedPackageRemove, view } = this.props;

        return (
            <ListGroupItem className="data-search__container" style={{height: '150px', overflowY: 'scroll'}}>
                <div className="data-search__row">
                    {Array.isArray(subPackages) && subPackages.length > 0 ?
                        subPackages.map((d, i) => {
                            return (
                                <div key={'data-search__row' + i}>
                                    <SuperPackageRow
                                        model={subPackages[i]}
                                        menuActionName="Remove"
                                        handleMenuAction={handleAssignedPackageRemove}
                                        view={view}
                                    />
                                </div>
                            )
                        })
                        : 'None'
                    }
                </div>
            </ListGroupItem>
        )
    }
}

export const SubpackageViewer = connect<any, any, SubpackageViewerProps>(null)(SubpackageViewerImpl);