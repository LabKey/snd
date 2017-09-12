import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom'

import { SND_PKG_QUERY, SND_PKG_SCHEMA } from '../constants'

import { SchemaQuery } from '../../../query/model'
import { QuerySearch } from '../../../query/QuerySearchInput'

import { PackageViewer } from './PackageViewer'
interface PackageSearchOwnProps extends RouteComponentProps<{}> {}

const schemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, SND_PKG_QUERY);

export class PackageSearch extends React.Component<PackageSearchOwnProps, any> {

    render() {
        return (
            <Panel>
                <QuerySearch schemaQuery={schemaQuery}>
                    <PackageViewer/>
                </QuerySearch>
            </Panel>
        )
    }
}