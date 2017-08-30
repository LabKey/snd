import { PKG_TYPES } from './constants'
import { PackageModel, PackagesModel } from './model'

import { labkeyAjax, selectRows } from '../../utils/query'

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

export function getPackages() {
    return (dispatch, getState) => {
        dispatch(packagesLoading());

        return selectRows('snd', 'pkgs').then((response: LabKeyQueryResponse) => {
            dispatch(packagesLoaded());
            dispatch(packagesSuccess(response))

        }).catch((error) => {
            // set error
            console.log('error', error)
        });
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

function packagesLoaded() {
    return {
        type: PKG_TYPES.PACKAGES_LOADED
    }
}

function packagesLoading() {
    return {
        type: PKG_TYPES.PACKAGES_LOADING
    };
}

function packageSuccess(response: PackageQueryResponse) {
    return {
        type: PKG_TYPES.PACKAGE_SUCCESS,
        response
    };
}

function packagesSuccess(response: LabKeyQueryResponse) {
    return {
        type: PKG_TYPES.PACKAGES_SUCCESS,
        response
    };
}

export function filterPackages(input: string) {
    return {
        type: PKG_TYPES.PACKAGES_SEARCH_FILTER,
        input
    };
}







export interface LabKeyQueryRowPropertyProps {
    displayValue?: string
    url?: string
    value: any
}

interface LabKeyQueryResponse {
    columnModel?: Array<{
        align: string,
        dataIndex: string,
        editable: boolean,
        header: string,
        hidden: boolean,
        required: boolean,
        scale: number,
        sortable: boolean,
        width: number
    }>
    metaData?: {[key: string]: any}
    queryName: string
    rowCount: number
    rows: Array<{data: {[key: string]: any}}>
    schemaKey: {name: string, parent: any}
    schemaName: Array<string> | string
}

interface PackageQueryResponse {
    json: Array<PackageModel>
}

