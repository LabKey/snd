import * as React from 'react';
import { ListGroupItem } from 'react-bootstrap'

import { QuerySuperPackageModel, AssignedPackageModel } from '../../containers/Packages/model'
import { SuperPackageRow } from './SuperPackageRow'
import { PACKAGE_VIEW } from '../../containers/Packages/Forms/PackageFormContainer'

interface SuperPackageSearchResultsProps {
    data: {[key: string]: any} // TODO: why doesn't QuerySuperPackageModel work here instead of any?
    dataIds: Array<number>
    isLoaded: boolean
    primitivesOnly: boolean,
    handleAssignedPackageAdd: (assignedPackage: AssignedPackageModel) => void
    view: PACKAGE_VIEW
}

export class SuperPackageSearchResults extends React.Component<SuperPackageSearchResultsProps, any> {

    static defaultProps: SuperPackageSearchResultsProps = {
        data: {},
        dataIds: [],
        isLoaded: false,
        primitivesOnly: false,
        handleAssignedPackageAdd: undefined,
        view: undefined
    };

    render() {
        const { data, dataIds, isLoaded, primitivesOnly, handleAssignedPackageAdd, view } = this.props;

        if (isLoaded && data && Array.isArray(dataIds)) {
            return (
                <ListGroupItem className="data-search__container" style={{height: '200px', overflowY: 'scroll'}}>
                    {dataIds.length > 0 ?
                        dataIds.filter((d) => {
                            const rowData: QuerySuperPackageModel = data[d];
                            return !primitivesOnly || rowData.IsPrimitive.value === 'true';
                        }).map((d, i) => {
                            const rowData: QuerySuperPackageModel = data[d];
                            let assignedPackage = new AssignedPackageModel(rowData.PkgId.value, rowData.PkgId.displayValue, rowData.SuperPkgId.value);

                            return (
                                <div key={'data-search__row' + i}>
                                    <SuperPackageRow
                                        model={assignedPackage}
                                        menuActionName="Add"
                                        handleMenuAction={handleAssignedPackageAdd}
                                        view={view}
                                    />
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