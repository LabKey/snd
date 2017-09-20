import { PKG_SQ, PKG_TYPES, SND_PKG_QUERY, SND_PKG_SCHEMA } from './constants'
import { PackagesModel } from './model'

import { deleteRows, queryInvalidate } from '../../query/actions'
import { QueryModel } from '../../query/model'

export function deletePackage(id: number) {
    return (dispatch) => {
        const rows = [{PkgId: id }];

        // todo: this should be wrapped in permissions check
        // should also display feedback to the user that the pkg was successfully deleted
        // need app wide 'message/error' field
        return deleteRows(SND_PKG_SCHEMA, SND_PKG_QUERY, rows).then((response) => {
            dispatch(queryInvalidate(PKG_SQ));
            dispatch(packagesInvalidate());
        }).catch((error) => {
            console.log('delete package error', error)
        });
    }
}

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

export function packagesInvalidate() {
    return {
        type: PKG_TYPES.PACKAGES_INVALIDATE
    };
}

export function packagesResetWarning() {
    return {
        type: PKG_TYPES.PACKAGES_RESET_WARNING
    }
}

export function packagesWarning(message?: string) {
    return {
        type: PKG_TYPES.PACKAGES_WARNING,
        message
    }
}

export function toggleDrafts() {
    return {
        type: PKG_TYPES.PACKAGES_TOGGLE_DRAFTS
    };
}