import { handleActions } from 'redux-actions';

import { PKG_TYPES } from './constants'
import { QueryPackageModel, PackagesModel } from './model'


export const packages = handleActions({

    [PKG_TYPES.PACKAGES_INIT]: (state: PackagesModel, action: any) => {
        const { dataResponse } = action;
        const { data, dataCount, dataIds } = dataResponse;

        let active = [],
            ids = [],
            drafts = [];
        const packagesData = dataIds.reduce((prev, next: number) => {
            const packageData = data[next];
            const id = packageData.PkgId.value;
            ids.push(id);
            prev[id] = new QueryPackageModel(packageData);
            // // should filter on hasEvent or Active?
            if (packageData.Active.value === true) {
                active.push(id);
            }
            else {
                drafts.push(id);
            }

            return prev;
        }, {});

        return new PackagesModel(Object.assign({}, state, {
            active,
            data: packagesData,
            dataIds: ids,
            drafts,
            isInit: true,
            filteredActive: active,
            filteredDrafts: drafts,
            packageCount: dataCount
        }));
    },

    [PKG_TYPES.PACKAGES_SEARCH_FILTER]: (state: PackagesModel, action: any) => {
        const { active, data, dataIds, drafts } = state;
        const { input } = action;

        let filteredActive = active,
            filteredDrafts = drafts;

        if (input && input !== '') {
            const filtered = filterPackages(input, dataIds, data);
            if (filtered.length) {
                filteredActive = filtered.filter((id) => {
                    return active.indexOf(id) !== -1;
                });

                filteredDrafts = filtered.filter((id) => {
                    return drafts.indexOf(id) !== -1;
                });
            }
        }

        return new PackagesModel(Object.assign({}, state, {
            filteredActive,
            filteredDrafts
        }));
    },

}, new PackagesModel());

function filterPackages(input: string, dataIds: Array<number> , data: {[key: string]: any}) {

    return dataIds.filter((id: number) => {
        const pkg: QueryPackageModel = data[id];

        if (pkg) {
            return (
                pkg.Description &&
                pkg.Description.value.indexOf(input) !== -1
            ) || (
                pkg.PkgId &&
                pkg.PkgId.value.toString().indexOf(input) !== -1
            )
        }

        return false;
    });

}