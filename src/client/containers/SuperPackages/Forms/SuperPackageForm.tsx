
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux'

import {AssignedPackageModel} from '../model';
import {PackageModel} from "../../Wizards/Packages/model";
import {ProjectModel} from "../../Wizards/Projects/model";
import {QuerySearch} from "../../../query/QuerySearch";
import {SUPERPKG_REQUIRED_COLUMNS, TOPLEVEL_SUPER_PKG_SQ} from "../constants";
import {SuperPackageViewer} from "./SuperPackageViewer";
import {SubpackageViewer} from "./SubpackageViewer";
import {ControlLabel, ListGroupItem} from "react-bootstrap";
import {VIEW_TYPES} from "../../App/constants";
import {toggleSubpackageActive} from "../../Wizards/Projects/actions";

interface SuperPackageFormOwnProps {
    handleFieldChange?: (name: string, value: any) => void
    handleAssignedPackageAdd: (assignedPackage: AssignedPackageModel) => void
    handleAssignedPackageRemove: (assignedPackage: AssignedPackageModel) => any
    handleAssignedPackageReorder: (assignedPackage: AssignedPackageModel, moveUp: boolean) => any
    handleFullNarrative?: (model: AssignedPackageModel, shouldQuery: boolean) => void
    handleWarning?: (warning?: string) => void
    model?: ProjectModel | PackageModel
    showActive: boolean
    view?: VIEW_TYPES
}

interface SuperPackageFormState {
    dispatch?: Dispatch<any>
}

interface SuperPackageFormStateProps {
    selectedSubPackage?: AssignedPackageModel
}

type SuperPackageFormProps = SuperPackageFormOwnProps & SuperPackageFormState;



export class SuperPackageFormImpl extends React.Component<SuperPackageFormProps, SuperPackageFormStateProps>
{

    constructor(props?: SuperPackageFormProps) {
        super(props);

        this.state = {
            selectedSubPackage: undefined
        };

        this.handleAssignedPackageClick = this.handleAssignedPackageClick.bind(this);
        this.handleToggleActiveAction = this.handleToggleActiveAction.bind(this);
    }

    handleAssignedPackageClick(assignedPackage: AssignedPackageModel) {
        let {selectedSubPackage} = this.state;

        let idProp = assignedPackage.superPkgId ? 'SuperPkgId' : 'altId';

        if (selectedSubPackage == undefined || selectedSubPackage[idProp] != assignedPackage[idProp]) {
            this.setState({selectedSubPackage: assignedPackage});
        }
        else {
            this.setState({selectedSubPackage: undefined});
        }
    }

    handleToggleActiveAction(subpackage: AssignedPackageModel) {
        const { dispatch, model } = this.props;

        dispatch(toggleSubpackageActive(subpackage, model));
    }

    render()
    {
        const {model, view, handleAssignedPackageAdd, handleAssignedPackageRemove, handleAssignedPackageReorder,
            handleFullNarrative, showActive} = this.props;
        const {selectedSubPackage} = this.state;
        const isReadyOnly = (view === VIEW_TYPES.PROJECT_VIEW || view === VIEW_TYPES.PACKAGE_VIEW
            || (view === VIEW_TYPES.PACKAGE_EDIT && model.hasEvent));

        return (
            <div className="row clearfix">
                {!isReadyOnly ?
                    <div className="col-sm-6">
                        <div className="row clearfix col-xs-12 margin-top">
                            <ControlLabel>Available Packages</ControlLabel>
                        </div>
                        <div className="row col-xs-12">
                            <QuerySearch
                                id='superPackageViewer'
                                modelProps={{requiredColumns: SUPERPKG_REQUIRED_COLUMNS.TOP_LEVEL_SUPER_PKG}}
                                schemaQuery={TOPLEVEL_SUPER_PKG_SQ}>
                                <SuperPackageViewer
                                    schemaQuery={TOPLEVEL_SUPER_PKG_SQ}
                                    handleAssignedPackageAdd={handleAssignedPackageAdd}
                                    handleFullNarrative={handleFullNarrative}
                                    view={view}/>
                            </QuerySearch>
                        </div>
                    </div>
                    : null
                }
                <div className={isReadyOnly ? "col-sm-12" : "col-sm-6"}>
                    <div className="row clearfix col-xs-12 margin-top">
                        <ControlLabel>Assigned Packages</ControlLabel>
                        <SubpackageViewer
                            subPackages={model.subPackages}
                            selectedSubPackage={selectedSubPackage}
                            handleAssignedPackageRemove={handleAssignedPackageRemove}
                            handleAssignedPackageReorder={handleAssignedPackageReorder}
                            handleRowClick={this.handleAssignedPackageClick}
                            handleToggleActiveAction={this.handleToggleActiveAction}
                            handleFullNarrative={handleFullNarrative}
                            showActive={showActive}
                            view={view}/>
                    </div>
                    <div className="row clearfix col-xs-12 margin-top">
                        <ListGroupItem className="data-search__container" style={{height: '90px', overflowY: 'auto'}}>
                            <div className="data-search__row">
                                {selectedSubPackage != undefined
                                    ? selectedSubPackage.narrative
                                    : <div className="narrative-none">
                                        Select a package to view its narrative.
                                    </div>
                                }
                            </div>
                        </ListGroupItem>
                    </div>
                </div>
            </div>
        );
    }
}

export const SuperPackageForm = connect<any, any, SuperPackageFormProps>(null)(SuperPackageFormImpl);