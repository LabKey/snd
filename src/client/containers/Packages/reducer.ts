import { handleActions } from 'redux-actions';

import { PKG_TYPES } from './constants'
import { QueryPackageModel, PackageModel, PackagesModel } from './model'


export const packages = handleActions({

    [PKG_TYPES.PACKAGE_LOADED]: (state: PackagesModel) => {

        return new PackagesModel(Object.assign({}, state, {
            packageLoaded: true,
            packageLoading: false
        }));
    },

    [PKG_TYPES.PACKAGE_LOADING]: (state: PackagesModel) => {
        return new PackagesModel(Object.assign({}, state, {
            packageLoaded: false,
            packageLoading: true
        }));
    },

    [PKG_TYPES.PACKAGES_LOADED]: (state: PackagesModel) => {

        return new PackagesModel(Object.assign({}, state, {
            isLoaded: true,
            isLoading: false
        }));
    },

    [PKG_TYPES.PACKAGES_LOADING]: (state: PackagesModel) => {
        return new PackagesModel(Object.assign({}, state, {
            isLoaded: false,
            isLoading: true
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

    [PKG_TYPES.PACKAGES_SUCCESS]: (state: PackagesModel, action: any) => {
        const { response } = action;

        let active = [],
            dataIds = [],
            drafts = [];
        const data = response.rows.reduce((prev, next: QueryPackageModel) => {

            const id = next.PkgId.value;
            dataIds.push(id);
            prev[id] = new QueryPackageModel(next);

            // should filter on hasEvent or Active?
            if (next.Active.value === true) {
                active.push(id);
            }
            else {
                drafts.push(id);
            }

            return prev;
        }, {});

        return new PackagesModel(Object.assign({}, state, {
            active,
            data,
            dataIds,
            drafts,
            filteredActive: active,
            filteredDrafts: drafts,
            packageCount: response.rowCount
        }));
    },

    [PKG_TYPES.PACKAGE_SUCCESS]: (state: PackagesModel, action: any) => {
        const { response } = action;
        const { json } = response;

        const packageData = json.reduce((prev, next: PackageModel) => {
            const id = next.pkgId;
            prev[id] = new PackageModel(next);

            return prev;
        }, {});

        return new PackagesModel(Object.assign({}, state, {
            packageData: Object.assign({}, state.packageData, packageData)
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