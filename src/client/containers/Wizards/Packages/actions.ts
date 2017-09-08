import { PKG_WIZARD_TYPES } from './constants'
import { PackageModel, PackageWizardModel } from './model'

import { labkeyAjax, querySelectRows } from '../../../query/actions'

export function fetchPackage(id: string | number) {
    return (dispatch, getState) => {
        let packageModel: PackageWizardModel = getState().wizards.packages.packageData[id];

        if (!packageModel) {
            dispatch(packageInit(id));
        }

        packageModel = getState().wizards.packages.packageData[id];

        if (!packageModel.packageLoaded && !packageModel.packageLoading) {
            dispatch(packageModel.loading());

            return labkeyAjax(
                'snd',
                'getPackages',
                null,
                {"packages":[id]}
            ).then((response: PackageQueryResponse) => {
                dispatch(packageModel.loaded());

                packageModel = getState().wizards.packages.packageData[id];
                dispatch(packageModel.success(response));
            }).catch((error) => {
                // set error
                console.log('error', error)
            });
        }
    }
}

export function packageInit(id) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_INIT,
        id
    }
}


export function packageError(model: PackageWizardModel, error: any) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_ERROR,
        error,
        model
    }
}

export function packageLoaded(model: PackageWizardModel) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_LOADED,
        model
    }
}

export function packageLoading(model: PackageWizardModel) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_LOADING,
        model
    };
}

export function packageSuccess(model: PackageWizardModel, response: PackageQueryResponse) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_SUCCESS,
        model,
        response
    };
}

export function saveNarrative(model: PackageWizardModel, narrative: string) {
    return {
        type: PKG_WIZARD_TYPES.SAVE_NARRATIVE,
        model,
        narrative
    }

}

export function parseNarrativeKeywords(narrative): Array<string> {
    const keywords = narrative.match(/[^{}]+(?=})/g);
    if (keywords && keywords.length) {
        return keywords;
    }

    return [];
}

interface PackageQueryResponse {
    json: Array<PackageModel>
}

