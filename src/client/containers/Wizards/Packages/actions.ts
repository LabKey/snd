import { PKG_WIZARD_TYPES } from './constants'
import { PackageModel, PackageWizardModel } from './model'

import { PACKAGE_VIEW } from '../../Packages/Forms/PackageFormContainer'
import { labkeyAjax } from '../../../query/actions'

export function fetchPackage(id: string | number) {
    return labkeyAjax(
        'snd',
        'getPackages',
        null,
        {"packages":[id]}
    );
}

export function init(id: string | number, view: PACKAGE_VIEW) {
    return (dispatch, getState) => {
        let packageModel: PackageWizardModel = getState().wizards.packages.packageData[id];

        if (!packageModel) {
            dispatch(initPackageModel(id));
        }

        packageModel = getState().wizards.packages.packageData[id];


        if (shouldFetch(packageModel, view)) {
            dispatch(packageModel.loading());

            return fetchPackage(id).then((response: PackageQueryResponse) => {

                packageModel = getState().wizards.packages.packageData[id];
                dispatch(packageModel.success(response));

                packageModel = getState().wizards.packages.packageData[id];
                dispatch(packageModel.loaded());
            }).catch((error) => {
                // set error
                console.log('error', error)
            });
        }
        else if (packageModel && !packageModel.packageLoaded && !packageModel.packageLoading) {
            dispatch(packageModel.loaded());
        }
    }
}

export function initPackageModel(id) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_INIT,
        id
    }
}

function shouldFetch(model: PackageWizardModel, view: PACKAGE_VIEW): boolean {
    return !model.packageLoaded && !model.packageLoading && view !== PACKAGE_VIEW.NEW;
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
    return (dispatch, getState) => {
        dispatch({
            type: PKG_WIZARD_TYPES.SAVE_NARRATIVE,
            model,
            narrative,
            parsedNarrative: parseNarrativeKeywords(narrative)
        });

        // Redux form will not change one field value from another automatically, do that here for the attribute name
    }
}

export function saveDraft() {

}

export function submitFinal() {

}

export function submitReview() {

}

export function parseNarrativeKeywords(narrative): Array<string> {

    let start,
        keyword = [],
        keywords = [];

    for (let char of narrative) {
        if (!start && char === '{') {
            start = true;
        }
        else if (start && char === '}') {
            start = false;
            keywords.push(keyword.join(''));
            keyword = [];
        }
        else if (start) {
            keyword.push(char);
        }
    }

    if (keywords && keywords.length) {
        return keywords;
    }

    return [];
}

interface PackageQueryResponse {
    json: Array<PackageModel>
}

export function save(values: {[key: string]: any}) {

    return savePackage(formatPackageValues(values)).then((response) => {
        console.log(response)
    }).catch((error) => {
        console.log('error', error)
    });
}

function formatPackageValues(values: {[key: string]: any}) {
    const {
        active,
        categories,
        description,
        narrative,
        pkgId,
        repeatable
    } = values;

    const attributes = Object.keys(values.attributes).map((attribute, i) => {
        const attributeVals = values.attributes[attribute];
        // loop through the attribute keys and strip off the _# like name_0 = name
        return Object.keys(attributeVals).reduce((prev, next) => {
            const nextKey = next.split('_')[0];
            prev[nextKey] = attributeVals[next];
            return prev;
        }, {});
    });

    return {
        active,
        attributes,
        categories,
        description,
        extraFields: {},
        id: pkgId,
        narrative,
        repeatable,
        subpackages: [],
    }
}

function savePackage(jsonData) {
    return new Promise((resolve, reject) => {
        LABKEY.Ajax.request({
            method: 'POST',
            url: LABKEY.ActionURL.buildURL('snd', 'savePackage.api'),
            jsonData,
            success: (data) => {
                resolve(data);
            },
            failure: (data) => {
                reject(data);
            }
        });
    })
}