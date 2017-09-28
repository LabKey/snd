import * as React from 'react';
import { Link } from 'react-router-dom'
import { OverlayTrigger, Tooltip, TooltipProps } from 'react-bootstrap'
import { QueryPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./PackageRow.css');

interface PackageRowProps {
    data: QueryPackageModel
    dataId: number
    handleDelete?: (rowId) => any
}

interface PackageRowStateProps {
    isHover: boolean
}

export class PackageRow extends React.Component<PackageRowProps, PackageRowStateProps> {

    static generateToolTip(tooltip: string, id: string): React.ReactElement<TooltipProps> {
        return <Tooltip id={id}>{tooltip}</Tooltip>;
    }

    constructor(props: PackageRowProps) {
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
        const { handleDelete } = this.props;
        const { Description, HasEvent, HasProject, PkgId } = this.props.data;
        const { isHover } = this.state;
        const canDelete = HasEvent.value === true || HasProject.value === true;

        // todo: URLs should be more resilient
        return (
            <div
                className={"package_viewer__result clearfix " + styles["package-row"]}
                data-packageId={PkgId.value}
                onMouseEnter={this.handleMouseEnter}
                onMouseLeave={this.handleMouseLeave}>
                <div className="result-icons pull-left">
                    <OverlayTrigger overlay={PackageRow.generateToolTip('View', PkgId.value)} placement="top">
                        <Link to={'/packages/view/' + PkgId.value} className={styles["package-row_icon"]}>
                            <i className={"fa fa-eye"}/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={PackageRow.generateToolTip('Edit', PkgId.value)} placement="top">
                        <Link to={'/packages/edit/' + PkgId.value} className={styles["package-row_icon"]}>
                            <i className="fa fa-pencil"/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={PackageRow.generateToolTip('Clone', PkgId.value)} placement="top">
                        <Link to={'/packages/clone/' + PkgId.value} className={styles["package-row_icon"]}>
                            <i className="fa fa-files-o"/>
                        </Link>
                    </OverlayTrigger>
                </div>
                <div className="pull-left " style={{marginLeft: '10px'}}>
                    {[PkgId.value, Description.value].join(' - ')}
                </div>
                {isHover && canDelete ?
                    <div className="pull-right" style={{cursor: 'pointer'}}>
                        <i className="fa fa-times" onClick={() => handleDelete(PkgId.value)}/>
                    </div>
                : null}

            </div>
        )
    }
}
