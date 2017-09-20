import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom'

import { QuerySearch } from '../../../query/QuerySearch'

import { PackageViewer } from './PackageViewer'
import { PKG_SQ, REQUIRED_COLUMNS } from '../constants'

interface PackageSearchOwnProps extends RouteComponentProps<{}> {}

export class PackageSearch extends React.Component<PackageSearchOwnProps, any> {

    render() {
        return (
            <Panel>
                <QuerySearch
                    id='packageSearch'
                    modelProps={{requiredColumns: REQUIRED_COLUMNS.PKGS}}
                    schemaQuery={PKG_SQ}>
                    <PackageViewer history={this.props.history}/>
                </QuerySearch>
            </Panel>
        )
    }
}