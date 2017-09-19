import * as React from 'react';
import { DropdownButton, MenuItem } from 'react-bootstrap'
import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'
import { AssignedPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./SuperPackageRow.css');

interface SuperPackageRowProps {
    model: AssignedPackageModel
    menuActionName?: string
    handleMenuAction?: (model: AssignedPackageModel) => any
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
    }

    handleMouseEnter() {
        this.setState({isHover: true});
    }

    handleMouseLeave() {
        this.setState({isHover: false});
    }

    render() {
        const { model, menuActionName, handleMenuAction, view } = this.props;
        const { isHover } = this.state;
        let isReadyOnly = view == PACKAGE_VIEW.VIEW;

        return (
            <div
                className={"superpackage_viewer__result clearfix " + styles["superpackage-row"]}
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}>
                <div className="pull-left " style={{marginLeft: '10px'}}>
                    {[model.PkgId, model.Description].join(' - ')}
                </div>
                {isHover ?
                    <div className="pull-right" style={{cursor: 'pointer'}}>
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
                    : null}

            </div>
        )
    }
}
