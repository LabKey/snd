import * as React from 'react';
import { ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import { PACKAGE_VIEW } from './PackageFormContainer'
import { SuperPackageRow } from '../../../components/SuperPackages/SuperPackageRow'
import { AssignedPackageModel } from "../model";

interface SubpackageViewerOwnProps {
    subPackages: Array<AssignedPackageModel>
    selectedSubPackage: AssignedPackageModel
    handleAssignedPackageRemove: (assignedPackage: AssignedPackageModel) => any
    handleRowClick: (assignedPackage: AssignedPackageModel) => any
    view?: PACKAGE_VIEW
}

interface SubpackageViewerState {
    dispatch?: Dispatch<any>
}

type SubpackageViewerProps = SubpackageViewerOwnProps & SubpackageViewerState;

export class SubpackageViewerImpl extends React.Component<SubpackageViewerProps, {}> {

    renderAssignedPackageRow(assignedPackage: AssignedPackageModel, allowRemove: boolean, treeLevel: number, key: string) {
        const { selectedSubPackage, handleAssignedPackageRemove, handleRowClick, view } = this.props;
        let idProp = selectedSubPackage != undefined && selectedSubPackage.SuperPkgId ? 'SuperPkgId' : 'altId';

        return (
            <div key={key}>
                <SuperPackageRow
                    model={assignedPackage}
                    selected={selectedSubPackage != undefined && assignedPackage[idProp] == selectedSubPackage[idProp]}
                    menuActionName={allowRemove ? "Remove" : null}
                    handleMenuAction={allowRemove ? handleAssignedPackageRemove : null}
                    handleRowClick={handleRowClick}
                    treeLevel={treeLevel}
                    view={view}
                />

                {Array.isArray(assignedPackage.SubPackages) && assignedPackage.SubPackages.length > 0
                    ? assignedPackage.SubPackages.map((dd, ii) => {
                        return this.renderAssignedPackageRow(assignedPackage.SubPackages[ii], false, treeLevel+1, key + "-" + ii);
                    })
                    : null
                }
            </div>
        )
    }

    render() {
        const { subPackages } = this.props;

        return (
            <ListGroupItem className="data-search__container" style={{height: '150px', overflowY: 'scroll'}}>
                <div className="data-search__row">
                    {Array.isArray(subPackages) && subPackages.length > 0 ?
                        subPackages.map((d, i) => {
                            return this.renderAssignedPackageRow(subPackages[i], true, 0, 'data-search__row' + i);
                        })
                        : 'None'
                    }
                </div>
            </ListGroupItem>
        )
    }
}

export const SubpackageViewer = connect<any, any, SubpackageViewerProps>(null)(SubpackageViewerImpl);