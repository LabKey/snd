import * as React from 'react';
import { Link } from 'react-router-dom'
import { Tooltip, OverlayTrigger } from 'react-bootstrap'
import { LabKeyQueryRowPropertyProps } from '../../query/model'

const styles = require<any>('./PackageRow.css');

interface PackageRowProps {
    data: {[key: string]: LabKeyQueryRowPropertyProps}
    dataId: number
}

export class PackageRow extends React.Component<PackageRowProps, {}> {

    render() {
        const resultStyle = {
            padding: '3px 10px',

        };

        const { Description, PkgId } = this.props.data;

        let tooltip = (tooltip: string) => {return <Tooltip id={PkgId.value}>{tooltip}</Tooltip>};

        // todo: URLs should be more resilient
        return (
            <div className={"package_viewer__result clearfix " + styles["package-row"]}>
                <div className="result-icons pull-left">
                    <OverlayTrigger overlay={tooltip("View")} placement="top">
                        <Link to={'packages/view/' + PkgId.value}>
                            <i className={"fa fa-eye " + styles["package-row_icon"]}/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={tooltip("Edit")} placement="top">
                        <Link to={'packages/edit/' + PkgId.value}>
                            <i className={"fa fa-pencil " + styles["package-row_icon"]}/>
                        </Link>
                    </OverlayTrigger>
                    <OverlayTrigger overlay={tooltip("Clone")} placement="top">
                        <Link to={'packages/clone/' + PkgId.value}>
                            <i className={"fa fa-files-o " + styles["package-row_icon"]}/>
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
