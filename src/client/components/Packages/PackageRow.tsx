import * as React from 'react';
import { Link } from 'react-router-dom'

import { LabKeyQueryRowPropertyProps } from '../../query/model'

interface PackageRowProps {
    data: {[key: string]: LabKeyQueryRowPropertyProps}
    dataId: number
}

export class PackageRow extends React.Component<PackageRowProps, {}> {

    render() {
        const resultStyle = {
            padding: '3px 10px'
        };

        const resultIconStyle = {
            marginRight: '10px',
            cursor: 'pointer'
        };

        const { Description, PkgId } = this.props.data;

        // todo: URLs should be more resilient
        return (
            <div className="package_viewer__result clearfix" style={resultStyle}>
                <div className="result-icons pull-left">
                    <Link to={'packages/view/' + PkgId.value}>
                        <i className="fa fa-eye" style={resultIconStyle}/>
                    </Link>
                    <Link to={'packages/edit/' + PkgId.value}>
                        <i className="fa fa-pencil" style={resultIconStyle}/>
                    </Link>
                    <Link to={'packages/clone/' + PkgId.value}>
                        <i className="fa fa-files-o" style={resultIconStyle}/>
                    </Link>
                </div>
                <div className="pull-left" style={{marginLeft: '20px'}}>
                    {[PkgId.value, Description.value].join(' - ')}
                </div>
            </div>
        )
    }
}
