import * as React from 'react';
import { Link } from 'react-router-dom'
import { OverlayTrigger, Tooltip, TooltipProps } from 'react-bootstrap'
import { QueryPackageModel } from '../../containers/Packages/model'

const styles = require<any>('./PackageRow.css');

interface PackageRowProps {
    data: QueryPackageModel
    dataId: number
}

export class PackageRow extends React.Component<PackageRowProps, {}> {

    static generateToolTip(tooltip: string, id: string): React.ReactElement<TooltipProps> {
        return <Tooltip id={id}>{tooltip}</Tooltip>;
    }

    render() {
        const { Description, PkgId } = this.props.data;

        // todo: URLs should be more resilient
        return (
            <div className={"package_viewer__result clearfix " + styles["package-row"]}>
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
            </div>
        )
    }
}
