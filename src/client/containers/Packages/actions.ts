import { PKG_TYPES } from './constants'
import { PackagesModel } from './model'

import { QueryModel } from '../../query/model'

export function filterPackages(input: string) {
    return {
        type: PKG_TYPES.PACKAGES_SEARCH_FILTER,
        input
    };
}

export function packagesInit(model: PackagesModel, dataResponse: QueryModel) {
    return {
        type: PKG_TYPES.PACKAGES_INIT,
        dataResponse,
        model
    };
}

