import * as React from 'react';
import { DropdownButton, MenuItem } from 'react-bootstrap'
import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'
import { AssignedPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./SuperPackageRow.css');

interface SuperPackageRowProps {
    model: AssignedPackageModel
    selected?: boolean
    handleRowClick?: (model: AssignedPackageModel) => any
    menuActionName?: string
    handleMenuAction?: (model: AssignedPackageModel) => any
    treeLevel?: number
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

    handleOnClick() {
        const { model, handleRowClick } = this.props;
        if (handleRowClick) {
            handleRowClick(model);
        }
    }

    render() {
        const { model, selected, menuActionName, handleMenuAction, treeLevel, view } = this.props;
        const { isHover } = this.state;
        let isReadyOnly = view == PACKAGE_VIEW.VIEW;
        let treeLevelVal = treeLevel == undefined ? -1 : treeLevel;
        let indentPx = treeLevel == undefined ? 0 : treeLevelVal * 15;

        let caretStyle = {width: '15px'};
        if (model.SubPackages.length == 0) {
            caretStyle['opacity'] = 0.7;
        }

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
                        ? model.SubPackages.length > 0
                            ? <i className="fa fa-caret-down" style={caretStyle}>&nbsp;</i>
                            : <i className="fa fa-caret-right" style={caretStyle}>&nbsp;</i>
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
                        <MenuItem disabled>Full Narrative</MenuItem>
                        <MenuItem disabled>Packages Using</MenuItem>
                        <MenuItem disabled>Projects Using</MenuItem>
                    </DropdownButton>
                </div>
            </div>
        )
    }
}
