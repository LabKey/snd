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
    handleAssignedPackageReorder: (assignedPackage: AssignedPackageModel, moveUp: boolean) => any
    handleRowClick: (assignedPackage: AssignedPackageModel) => any
    handleFullNarrative: (model: AssignedPackageModel, shouldQuery: boolean) => void
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
        this.handleAssignedFullNarrative = this.handleAssignedFullNarrative.bind(this);
    }

    handleIconClick(model: AssignedPackageModel) {
        let { collapsed } = this.state;

        if (model) {
            let id = this.getModelId(model);

            if (collapsed[id] != undefined) {
                collapsed[id] = !collapsed[id];
            }
            else {
                collapsed[id] = true;
            }
        }

        this.setState({collapsed});
    }

    handleAssignedFullNarrative(model: AssignedPackageModel) {
        const { handleFullNarrative } = this.props;
        handleFullNarrative(model, false);
    }

    getModelId(model: AssignedPackageModel) {
        if (model.altId == undefined) {
            model.altId = LABKEY.Utils.id();
        }

        return model.altId;
    }

    renderAssignedPackageRow(assignedPackage: AssignedPackageModel, key: string, treeLevel: number, arrIndex: number, arrLength: number) {
        const { selectedSubPackage, handleAssignedPackageRemove, handleAssignedPackageReorder, handleRowClick, view } = this.props;
        const { collapsed } = this.state;
        const idProp = selectedSubPackage != undefined && selectedSubPackage.SuperPkgId ? 'SuperPkgId' : 'altId';
        const treeCollapsed = collapsed[this.getModelId(assignedPackage)] || false;
        const isTopLevelSubpackage = treeLevel == 0;

        return (
            <div key={key}>
                <SuperPackageRow
                    model={assignedPackage}
                    selected={selectedSubPackage != undefined && assignedPackage[idProp] == selectedSubPackage[idProp]}
                    menuActionName={isTopLevelSubpackage ? "Remove" : null}
                    handleMenuAction={isTopLevelSubpackage ? handleAssignedPackageRemove : null}
                    handleMenuReorderAction={isTopLevelSubpackage ? handleAssignedPackageReorder : null}
                    handleIconClick={this.handleIconClick}
                    handleRowClick={handleRowClick}
                    handleFullNarrative={this.handleAssignedFullNarrative}
                    treeLevel={treeLevel}
                    treeArrIndex={arrIndex}
                    treeArrLength={arrLength}
                    treeCollapsed={treeCollapsed}
                    view={view}
                />

                {!treeCollapsed && Array.isArray(assignedPackage.SubPackages) && assignedPackage.SubPackages.length > 0
                    ? assignedPackage.SubPackages.map((dd, ii) => {
                        return this.renderAssignedPackageRow(
                            assignedPackage.SubPackages[ii], key + "-" + ii,
                            treeLevel+1, ii, assignedPackage.SubPackages.length
                        );
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
                            return this.renderAssignedPackageRow(subPackages[i], 'data-search__row' + i, 0, i, subPackages.length);
                        })
                        : 'None'
                    }
                </div>
            </ListGroupItem>
        )
    }
}

export const SubpackageViewer = connect<any, any, SubpackageViewerProps>(null)(SubpackageViewerImpl);