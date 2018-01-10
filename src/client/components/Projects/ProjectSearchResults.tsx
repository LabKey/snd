

import * as React from 'react';

import { QueryProjectModel } from '../../containers/Projects/model'
import { ProjectRow } from './ProjectRow'

interface ProjectSearchResultsProps {
    data: {[key: string]: QueryProjectModel}
    dataIds: Array<number>
    isLoaded: boolean
    handleDelete?: (id, rev, objId) => any
}

export class ProjectSearchResults extends React.Component<ProjectSearchResultsProps, any> {

    static defaultProps: ProjectSearchResultsProps = {
        data: {},
        dataIds: [],
        isLoaded: false
    };

    render() {
        const { data, dataIds, isLoaded, handleDelete } = this.props;

        if (isLoaded && data && dataIds.length) {
            return (
                <div className="data-search__container">
                    {dataIds.map((d, i) => {
                        const rowData: QueryProjectModel = data[d];
                        return (
                            <div key={'data-search__row' + i}>
                                <ProjectRow data={rowData} dataId={d} handleDelete={handleDelete}/>
                            </div>
                        )
                    })}
                </div>
            )
        }
        else if (isLoaded && dataIds.length === 0) {
            return (
                <div className="data-search__container">
                    <div className="data-search__row">
                        No results found
                    </div>
                </div>
            )
        }

        return null;
    }
}