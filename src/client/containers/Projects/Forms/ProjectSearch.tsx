import * as React from 'react';
import { Panel } from 'react-bootstrap';
import { RouteComponentProps } from 'react-router-dom'

import { QuerySearch } from '../../../query/QuerySearch'

import { ProjectViewer } from './ProjectViewer'
import { PROJECT_SQL, REQUIRED_COLUMNS } from '../constants'

interface ProjectSearchOwnProps extends RouteComponentProps<{}> {}

export class ProjectSearch extends React.Component<ProjectSearchOwnProps, any> {

    render() {
        return (
            <Panel>
                <QuerySearch
                    id='projectSearch'
                    modelProps={{requiredColumns: REQUIRED_COLUMNS.PROJECTS}}
                    schemaQuery={PROJECT_SQL}>
                    <ProjectViewer history={this.props.history}/>
                </QuerySearch>
            </Panel>
        )
    }
}