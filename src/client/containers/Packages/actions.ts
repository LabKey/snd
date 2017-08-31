import { PKG_TYPES } from './constants'
import { PackageModel, PackagesModel } from './model'

import { labkeyAjax } from '../../query/actions'
import { QueryModel } from '../../query/model'

export function getPackage(id: string | number) {
    return (dispatch, getState) => {

        const packagesModel: PackagesModel = getState().packages;

        if (!packagesModel.packageLoaded && !packagesModel.packageLoading) {
            dispatch(packageLoading());

            return labkeyAjax(
                'snd',
                'getPackages',
                null,
                {"packages":[id]}
            ).then((response: PackageQueryResponse) => {
                // cannot set loaded as there could be more packages
                dispatch(packageLoaded());
                dispatch(packageSuccess(response));
            }).catch((error) => {
                // set error
                console.log('error', error)
            });
        }
    }
}

function packageLoaded() {
    return {
        type: PKG_TYPES.PACKAGE_LOADED
    }
}

function packageLoading() {
    return {
        type: PKG_TYPES.PACKAGE_LOADING
    };
}

function packageSuccess(response: PackageQueryResponse) {
    return {
        type: PKG_TYPES.PACKAGE_SUCCESS,
        response
    };
}

export function filterPackages(input: string) {
    return {
        type: PKG_TYPES.PACKAGES_SEARCH_FILTER,
        input
    };
}

export function packagesInit(model: PackagesModel, dataResponse: QueryModel) {
    return {
        type: PKG_TYPES.PACKAGE_INIT,
        dataResponse,
        model
    };
}


interface PackageQueryResponse {
    json: Array<PackageModel>
}

