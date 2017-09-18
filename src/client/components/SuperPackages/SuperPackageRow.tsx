import * as React from 'react';
import { DropdownButton, MenuItem } from 'react-bootstrap'
import { QuerySuperPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./SuperPackageRow.css');

interface SuperPackageRowProps {
    data: QuerySuperPackageModel
    dataId: number
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
        const { SuperPkgId, PkgId } = this.props.data;
        const { isHover } = this.state;

        return (
            <div
                className={"superpackage_viewer__result clearfix " + styles["superpackage-row"]}
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}>
                <div className="pull-left " style={{marginLeft: '10px'}}>
                    {[SuperPkgId.value, PkgId.displayValue].join(' - ')}
                </div>
                {isHover ?
                    <div className="pull-right" style={{cursor: 'pointer'}}>
                        <DropdownButton id="superpackage-actions" title="" pullRight className={styles["superpackage-row-option-btn"]}>
                            <MenuItem disabled>Add</MenuItem>
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
