/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from 'react';
import { DropdownButton, MenuItem } from 'react-bootstrap'
import {AssignedPackageModel} from "../../containers/SuperPackages/model";
import {VIEW_TYPES} from "../../containers/App/constants";

interface SuperPackageRowProps {
    model: AssignedPackageModel
    selected?: boolean
    handleMenuReorderAction?: (model: AssignedPackageModel, moveUp: boolean) => any
    handleIconClick?: (model: AssignedPackageModel) => any
    handleRowClick?: (model: AssignedPackageModel) => any
    menuActionName?: string
    handleMenuAction?: (model: AssignedPackageModel) => any
    handleFullNarrative?: (model: AssignedPackageModel) => void
    treeLevel?: number
    treeArrIndex?: number
    treeArrLength?: number
    treeCollapsed?: boolean
    showActive?: boolean
    active?: boolean
    view?: VIEW_TYPES
}

interface SuperPackageRowStateProps {
    isHover: boolean
    isDropup: boolean
}

export class SuperPackageRow extends React.Component<SuperPackageRowProps, SuperPackageRowStateProps> {

    constructor(props: SuperPackageRowProps) {
        super(props);

        this.state = {
            isHover: false,
            isDropup: false
        };

        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
        this.handleOnClick = this.handleOnClick.bind(this);
    }

    handleMouseEnter(e) {
        // use dropup for bottom half of the container
        const target = jQuery(e.target);
        const containerHeight = target.closest('.data-search__container').height();
        const isDropup = target.position().top > (containerHeight / 2);

        this.setState({isHover: true, isDropup});
    }

    handleMouseLeave() {
        this.setState({isHover: false, isDropup: false});
    }

    handleOnClick(evnt) {
        const { model, handleIconClick, handleRowClick } = this.props;
        let iconClick = evnt.target.getAttribute('class') && evnt.target.getAttribute('class').indexOf('icon-tree-toggle') > -1;

        if (!iconClick && handleRowClick) {
            handleRowClick(model);
        }

        if (iconClick && handleIconClick) {
            handleIconClick(model);
        }
    }

    render() {
        const {
            model, selected, treeLevel, treeArrIndex, treeArrLength, treeCollapsed, showActive, active,
            menuActionName, handleMenuAction, handleMenuReorderAction, handleFullNarrative, view
        } = this.props;
        const { isHover, isDropup } = this.state;
        const isReadyOnly = view == VIEW_TYPES.PACKAGE_VIEW;
        const treeLevelVal = treeLevel == undefined ? -1 : treeLevel;
        const treeCollapsedVal = treeCollapsed != undefined && treeCollapsed;
        const indentPx = treeLevel == undefined ? 0 : treeLevelVal * 15;

        // boolean to indicate if the reorder up/down menu items should be shown
        const showMoveUp = !isReadyOnly && handleMenuReorderAction && treeArrIndex != undefined && treeArrIndex > 0;
        const showMoveDown = !isReadyOnly && handleMenuReorderAction && treeArrIndex != undefined && treeArrLength != undefined && treeArrIndex < (treeArrLength - 1);

        // TODO: Remove this after design testing
        // let divStyle = {
        //     margin: '3px 10px 0 0',
        //     opacity: .5
        // };
        //
        // let icon = 'pull-left fa fa-eye';
        // var random_boolean = Math.random() >= 0.5;
        // if (random_boolean)
        // {
        //     icon = 'pull-left fa fa-eye-slash';
        //     divStyle.opacity = 1;
        // }

        return (

            <div
                className={"superpackage_viewer__result clearfix superpackage-row" + (selected ? ' superpackage-selected-row' : '')}
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}
                onClick={this.handleOnClick}
            >
                {/*<div className={icon} style={divStyle}></div>*/}
                <div className="pull-left" style={{marginLeft: indentPx + 'px'}}>
                    {treeLevelVal > -1
                        ? model.loadingSubpackages != undefined && model.loadingSubpackages === true
                            ? <i className="fa fa-spinner fa-spin">&nbsp;</i>
                            : !treeCollapsedVal && model.SubPackages.length > 0
                                ? <i className="icon-tree-toggle fa fa-caret-down">&nbsp;</i>
                                : model.SubPackages.length == 0
                                    ? <i className={"icon-tree-toggle fa fa-circle superpackage-row-leaf"}>&nbsp;&nbsp;&nbsp;</i>
                                    : <i className="icon-tree-toggle fa fa-caret-right">&nbsp;</i>
                        : null
                    }
                    {[model.PkgId, model.Description].join(' - ')}
                </div>
                <div className="superpackage-row-dropdown" style={{display: isHover ? 'inline-block' : 'none'}}>
                    <DropdownButton id="superpackage-actions" title="" pullRight dropup={isDropup}
                                    className="superpackage-row-option-btn">
                        {!isReadyOnly && menuActionName
                            ? <MenuItem onClick={() => handleMenuAction(model)}>{menuActionName}</MenuItem>
                            : null
                        }
                        {showMoveUp
                            ? <MenuItem onClick={() => handleMenuReorderAction(model, true)}>Move Up</MenuItem>
                            : null
                        }
                        {showMoveDown
                            ? <MenuItem onClick={() => handleMenuReorderAction(model, false)}>Move Down</MenuItem>
                            : null
                        }
                        {/*{showActive*/}
                            {/*? <MenuItem>Display<i className="fa fa-check">&nbsp;</i></MenuItem>*/}
                            {/*: <MenuItem>Display</MenuItem>*/}
                        {/*}*/}

                        <MenuItem onClick={() => handleFullNarrative(model)}>Full Narrative</MenuItem>
                        {/*<MenuItem disabled>Packages Using</MenuItem>*/}
                        {/*<MenuItem disabled>Projects Using</MenuItem>*/}
                    </DropdownButton>
                </div>
            </div>
        )
    }
}
