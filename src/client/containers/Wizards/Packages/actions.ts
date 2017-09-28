import { PKG_WIZARD_TYPES } from './constants'
import { PackageModel, PackageModelAttribute, PackageWizardModel, PackageSubmissionModel } from './model'

import { PACKAGE_VIEW } from '../../Packages/Forms/PackageFormContainer'
import { labkeyAjax, queryInvalidate } from '../../../query/actions'
import { packagesInvalidate } from '../../Packages/actions'
import { PKG_SQ, TOPLEVEL_SUPER_PKG_SQ } from '../../Packages/constants'

function fetchPackage(id: string | number, includeExtraFields: boolean, includeLookups: boolean) {
    return labkeyAjax(
        'snd',
        'getPackages',
        null,
        {
            'packages': [id],
            'excludeExtraFields': !includeExtraFields,
            "excludeLookups": !includeLookups
        }
    );
}

export function querySubPackageDetails(id: number, parentPkgId: number) {
    return (dispatch, getState) => {
        return fetchPackage(id, false, false).then((response: PackageQueryResponse) => {
            const parentPackageModel = getState().wizards.packages.packageData[parentPkgId];

            // the response should have exactly one row
            let responseData: PackageModel = Array.isArray(response.json) && response.json.length == 1 ? response.json[0] : {};

            let newSubpackages = parentPackageModel.data.subPackages.map((subPackage) => {
                if (subPackage.PkgId == responseData.pkgId) {
                    subPackage.SubPackages = responseData.subPackages;
                    subPackage.loadingSubpackages = undefined;
                }
                return subPackage;
            });

            dispatch(parentPackageModel.saveField('subPackages', newSubpackages));
        }).catch((error) => {
            // set error
            console.log('error', error)
        });
    }
}

export function init(id: string | number, view: PACKAGE_VIEW) {
    return (dispatch, getState) => {
        let packageModel: PackageWizardModel = getState().wizards.packages.packageData[id];

        // todo: make this cleaner - works for now
        if (!packageModel || packageModel.formView !== view) {
            const packageModelProps = {
                packageId: id,
                formView: view
            };
            dispatch(initPackageModel(packageModelProps));


            const model = getState().wizards.packages.packageData[id];
            if (packageModel && packageModel.formView !== view && view !== PACKAGE_VIEW.VIEW) {
                dispatch(model.checkValid());
            }
        }

        packageModel = getState().wizards.packages.packageData[id];

        if (shouldFetch(packageModel)) {
            dispatch(packageModel.loading());

            return fetchPackage(id, true, true).then((response: PackageQueryResponse) => {

                packageModel = getState().wizards.packages.packageData[id];
                dispatch(packageModel.success(response, view));

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

export function initPackageModel(props: {[key: string]: any}) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_INIT,
        props
    }
}

function shouldFetch(model: PackageWizardModel): boolean {
    return !model.packageLoaded && !model.packageLoading;
}

export function packageCheckValid(model: PackageWizardModel) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_CHECK_VALID,
        model
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

export function packageSuccess(model: PackageWizardModel, response: PackageQueryResponse, view: PACKAGE_VIEW) {
    return {
        type: PKG_WIZARD_TYPES.PACKAGE_SUCCESS,
        model,
        response,
        view
    };
}

export function saveNarrative(model: PackageWizardModel, narrative: string) {
    return (dispatch) => {
        dispatch({
            type: PKG_WIZARD_TYPES.SAVE_NARRATIVE,
            model,
            narrative
        });
    }
}

export function saveField(model: PackageWizardModel, name: string, value: any) {
    return {
        type: PKG_WIZARD_TYPES.SAVE_FIELD,
        model,
        name,
        value
    };
}

export function setSubmitted(model: PackageWizardModel) {
    return {
        type: PKG_WIZARD_TYPES.SET_SUBMITTED,
        model
    };
}

export function setSubmitting(model: PackageWizardModel) {
    return {
        type: PKG_WIZARD_TYPES.SET_SUBMITTING,
        model
    };
}

interface PackageQueryResponse {
    json: Array<PackageModel>
}

export function save(model: PackageWizardModel, pkg: PackageSubmissionModel, onSuccess?: any) {
    return (dispatch, getState) => {
        dispatch(setSubmitting(model));

        return savePackage(pkg).then((response) => {
            const updatedModel = getState().wizards.packages.packageData[model.packageId];

            dispatch(setSubmitted(updatedModel));
            dispatch(packagesInvalidate());
            dispatch(queryInvalidate(PKG_SQ));
            dispatch(queryInvalidate(TOPLEVEL_SUPER_PKG_SQ));
            onSuccess('/packages');
        }).catch((error) => {
            // todo handle errors
            console.log('error', error)
        });
    }
}

export function formatPackageValues(model: PackageWizardModel, active: boolean): PackageSubmissionModel {
    const { formView } = model;
    const { categories, description, extraFields, narrative, pkgId, repeatable } = model.data;
    let id;
    if ((formView !== PACKAGE_VIEW.CLONE) && (formView !== PACKAGE_VIEW.NEW)) {
        id = pkgId;
    }

    const attributes = model.data.attributes.map((attribute, i) => {
        // loop through the attribute keys and strip off the _# like name_0 = name
        return new PackageModelAttribute(Object.keys(attribute).reduce((prev, next) => {
            const nextKey = next.split('_')[0];
            if (next === 'lookupKey') {
                if (attribute[next]) {
                    const splitKey = attribute[next].split('.');
                    if (splitKey && splitKey.length > 1) {
                        prev['lookupSchema'] = splitKey[0];
                        prev['lookupQuery'] = splitKey[1];
                    }
                }
                else {
                    prev['lookupSchema'] = "";
                    prev['lookupQuery'] = "";
                }
            }
            else if (next === 'required') {
                prev['required'] = attribute[next] === 'on';
            }
            else {
                prev[nextKey] = attribute[next];
            }

            return prev;
        }, {}));
    });

    return new PackageSubmissionModel({
        active,
        attributes,
        categories,
        description,
        extraFields,
        id,
        narrative,
        repeatable,
        subPackages: [],
    });
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