import * as React from 'react';
import { ListGroupItem } from 'react-bootstrap'

import { QuerySuperPackageModel } from '../../containers/Packages/model'
import { SuperPackageRow } from './SuperPackageRow'

interface SuperPackageSearchResultsProps {
    data: {[key: string]: any} // TODO Why doesn't QuerySuperPackageModel work here instead of any?
    dataIds: Array<number>
    isLoaded: boolean
}

export class SuperPackageSearchResults extends React.Component<SuperPackageSearchResultsProps, any> {

    static defaultProps: SuperPackageSearchResultsProps = {
        data: {},
        dataIds: [],
        isLoaded: false
    };

    render() {
        const { data, dataIds, isLoaded } = this.props;

        if (isLoaded && data && Array.isArray(dataIds)) {
            return (
                <ListGroupItem className="data-search__container" style={{height: '200px', overflowY: 'scroll'}}>
                    {dataIds.length > 0 ?
                        dataIds.map((d, i) => {
                            const rowData: QuerySuperPackageModel = data[d];
                            return (
                                <div key={'data-search__row' + i}>
                                    <SuperPackageRow data={rowData} dataId={d}/>
                                </div>
                            )
                        })
                        : 'No results found'
                    }
                </ListGroupItem>
            )
        }

        return null;
    }
}