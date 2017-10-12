import * as React from 'react';
import { DropdownButton, MenuItem } from 'react-bootstrap'
import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'
import { AssignedPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./SuperPackageRow.css');

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
    view?: PACKAGE_VIEW
}

interface SuperPackageRowStateProps {
    isHover: boolean
}

export class SuperPackageRow extends React.Component<SuperPackageRowProps, SuperPackageRowStateProps> {

    constructor(props: SuperPackageRowProps) {
        super(props);

        this.state = {
            isHover: false
        };

        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
        this.handleOnClick = this.handleOnClick.bind(this);
    }

    handleMouseEnter() {
        this.setState({isHover: true});
    }

    handleMouseLeave() {
        this.setState({isHover: false});
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
            model, selected, treeLevel, treeArrIndex, treeArrLength, treeCollapsed,
            menuActionName, handleMenuAction, handleMenuReorderAction, handleFullNarrative, view
        } = this.props;
        const { isHover } = this.state;
        const isReadyOnly = view == PACKAGE_VIEW.VIEW;
        const treeLevelVal = treeLevel == undefined ? -1 : treeLevel;
        const treeCollapsedVal = treeCollapsed != undefined && treeCollapsed;
        const indentPx = treeLevel == undefined ? 0 : treeLevelVal * 15;

        // boolean to indicate if the reorder up/down menu items should be shown
        const showMoveUp = handleMenuReorderAction && treeArrIndex != undefined && treeArrIndex > 0;
        const showMoveDown = handleMenuReorderAction && treeArrIndex != undefined && treeArrLength != undefined && treeArrIndex < (treeArrLength - 1);

        return (
            <div
                className={"superpackage_viewer__result clearfix "
                            + styles["superpackage-row"]
                            + (selected ? ' ' + styles["superpackage-selected-row"] : '')
                          }
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}
                onClick={this.handleOnClick}
            >
                <div className="pull-left" style={{marginLeft: indentPx + 'px'}}>
                    {treeLevelVal > -1
                        ? model.loadingSubpackages != undefined && model.loadingSubpackages === true
                            ? <i className="fa fa-spinner fa-spin">&nbsp;</i>
                            : !treeCollapsedVal && model.SubPackages.length > 0
                                ? <i className="icon-tree-toggle fa fa-caret-down">&nbsp;</i>
                                : model.SubPackages.length == 0
                                    ? <i className={"icon-tree-toggle fa fa-circle " + styles["superpackage-row-leaf"]}>&nbsp;&nbsp;&nbsp;</i>
                                    : <i className="icon-tree-toggle fa fa-caret-right">&nbsp;</i>
                        : null
                    }
                    {[model.PkgId, model.Description].join(' - ')}
                </div>
                <div className={styles["superpackage-row-dropdown"]} style={{display: isHover ? 'inline-block' : 'none'}}>
                    <DropdownButton id="superpackage-actions" title="" pullRight className={styles["superpackage-row-option-btn"]}>
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
                        <MenuItem onClick={() => handleFullNarrative(model)}>Full Narrative</MenuItem>
                        {/*<MenuItem disabled>Packages Using</MenuItem>*/}
                        {/*<MenuItem disabled>Projects Using</MenuItem>*/}
                    </DropdownButton>
                </div>
            </div>
        )
    }
}
