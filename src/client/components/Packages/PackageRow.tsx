import * as React from 'react';


import { QueryPackageModel } from '../../containers/Packages/model'

interface PackageRowProps {
    pkg?: QueryPackageModel
}


interface PackageViewerStateProps {
    showDrafts: boolean
}


export class PackageRow extends React.Component<PackageRowProps, PackageViewerStateProps> {

    constructor(props?: PackageRowProps) {
        super(props);

        this.state = {
            showDrafts: false
        }
    }

    render() {
        const resultStyle = {
            padding: '3px 10px'
        };

        const resultIconStyle = {
            marginRight: '10px',
            cursor: 'pointer'
        };

        const { Description, PkgId } = this.props.pkg;

        return (
            <div className="package_viewer__result clearfix" style={resultStyle}>
                <div className="result-icons pull-left">
                    <i className="fa fa-eye" style={resultIconStyle}/>
                    <i className="fa fa-pencil" style={resultIconStyle}/>
                    <i className="fa fa-files-o" style={resultIconStyle}/>
                </div>
                <div className="pull-left" style={{marginLeft: '20px'}}>
                    {[PkgId.value, Description.value].join(' - ')}
                </div>
            </div>
        )
    }
}
