import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom'

import { QuerySearch } from '../../../query/QuerySearchInput'

import { PackageViewer } from './PackageViewer'
import { schemaQuery as PKG_SQ } from '../model'
interface PackageSearchOwnProps extends RouteComponentProps<{}> {}

export class PackageSearch extends React.Component<PackageSearchOwnProps, any> {

    render() {
        return (
            <Panel>
                <QuerySearch schemaQuery={PKG_SQ}>
                    <PackageViewer/>
                </QuerySearch>
            </Panel>
        )
    }
}