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
    treeLevel?: number
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
            model, selected, treeLevel, treeCollapsed, view,
            menuActionName, handleMenuAction, handleMenuReorderAction
        } = this.props;
        const { isHover } = this.state;
        let isReadyOnly = view == PACKAGE_VIEW.VIEW;
        let treeLevelVal = treeLevel == undefined ? -1 : treeLevel;
        let treeCollapsedVal = treeCollapsed != undefined && treeCollapsed;
        let indentPx = treeLevel == undefined ? 0 : treeLevelVal * 15;

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
                                    ? <i className={"icon-tree-toggle fa fa-circle " + styles["superpackage-row-leaf"]}>&nbsp;</i>
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
                        {handleMenuReorderAction
                            ? <MenuItem onClick={() => handleMenuReorderAction(model, true)}>Move Up</MenuItem>
                            : null
                        }
                        {handleMenuReorderAction
                            ? <MenuItem onClick={() => handleMenuReorderAction(model, false)}>Move Down</MenuItem>
                            : null
                        }
                        <MenuItem disabled>Full Narrative</MenuItem>
                        <MenuItem disabled>Packages Using</MenuItem>
                        <MenuItem disabled>Projects Using</MenuItem>
                    </DropdownButton>
                </div>
            </div>
        )
    }
}
