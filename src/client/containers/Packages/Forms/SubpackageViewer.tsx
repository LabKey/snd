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

interface SubpackageViewerStateProps {
    collapsed: {[key: string]: boolean}
}

type SubpackageViewerProps = SubpackageViewerOwnProps & SubpackageViewerState;

export class SubpackageViewerImpl extends React.Component<SubpackageViewerProps, SubpackageViewerStateProps> {

    constructor(props: SubpackageViewerProps) {
        super(props);

        this.state = {
            collapsed: {}
        };

        this.handleIconClick = this.handleIconClick.bind(this);
    }

    handleIconClick(model: AssignedPackageModel) {
        let { collapsed } = this.state;

        if (model) {
            let id = this.getModelId(model);
            // TODO issue with adding the same pkg to the tree having subpackages that get the same id

            if (collapsed[id] != undefined) {
                collapsed[id] = !collapsed[id];
            }
            else {
                collapsed[id] = true;
            }
        }

        this.setState({collapsed});
    }

    getModelId(model: AssignedPackageModel) {
        if (model.altId == undefined) {
            model.altId = LABKEY.Utils.id();
        }

        return model.altId;
    }

    renderAssignedPackageRow(assignedPackage: AssignedPackageModel, allowRemove: boolean, treeLevel: number, key: string) {
        const { selectedSubPackage, handleAssignedPackageRemove, handleRowClick, view } = this.props;
        let { collapsed } = this.state;
        let idProp = selectedSubPackage != undefined && selectedSubPackage.SuperPkgId ? 'SuperPkgId' : 'altId';
        let treeCollapsed = collapsed[this.getModelId(assignedPackage)] || false;

        return (
            <div key={key}>
                <SuperPackageRow
                    model={assignedPackage}
                    selected={selectedSubPackage != undefined && assignedPackage[idProp] == selectedSubPackage[idProp]}
                    menuActionName={allowRemove ? "Remove" : null}
                    handleMenuAction={allowRemove ? handleAssignedPackageRemove : null}
                    handleIconClick={this.handleIconClick}
                    handleRowClick={handleRowClick}
                    treeLevel={treeLevel}
                    treeCollapsed={treeCollapsed}
                    view={view}
                />

                {!treeCollapsed && Array.isArray(assignedPackage.SubPackages) && assignedPackage.SubPackages.length > 0
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