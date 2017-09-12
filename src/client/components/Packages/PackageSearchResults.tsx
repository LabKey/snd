import * as React from 'react';

import { QueryPackageModel } from '../../containers/Packages/model'
import { PackageRow } from './PackageRow'

interface PackageSearchResultsProps {
    data: {[key: string]: QueryPackageModel}
    dataIds: Array<number>
    isLoaded: boolean
}

export class PackageSearchResults extends React.Component<PackageSearchResultsProps, any> {

    static defaultProps: PackageSearchResultsProps = {
        data: {},
        dataIds: [],
        isLoaded: false
    };

    render() {
        const { data, dataIds, isLoaded } = this.props;

        if (isLoaded && data && dataIds.length) {
            return (
                <div className="data-search__container">
                    {dataIds.map((d, i) => {
                        const rowData: QueryPackageModel = data[d];
                        return (
                            <div key={'data-search__row' + i}>
                                <PackageRow data={rowData} dataId={d}/>
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