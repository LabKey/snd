import { handleActions } from 'redux-actions';

import { PKG_WIZARD_TYPES } from './constants'
import {
    defaultPackageModelAttribute,
    defaultPackageWizardModel,
    PackageModelAttribute,
    PackageModel,
    PackageWizardModel,
    PackageWizardContainer
} from './model'
import {PACKAGE_VIEW} from "../../Packages/Forms/PackageFormContainer";

export const packages = handleActions({

    [PKG_WIZARD_TYPES.PACKAGE_ERROR]: (state: PackageWizardContainer, action: any) => {
        //const { model } = action;

        return state;
    },

    [PKG_WIZARD_TYPES.PACKAGE_INIT]: (state: PackageWizardContainer, action: any) => {
        const { id } = action;
        const model = new PackageWizardModel(Object.assign({}, defaultPackageWizardModel,{packageId: id}));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [id]: model
        }}));
    },

    [PKG_WIZARD_TYPES.PACKAGE_LOADED]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new PackageWizardModel(Object.assign({}, model, {
            packageLoaded: true,
            packageLoading: false
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [loadingModel.packageId]: loadingModel
        }}));
    },

    [PKG_WIZARD_TYPES.PACKAGE_LOADING]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new PackageWizardModel(Object.assign({}, model, {
            packageLoaded: false,
            packageLoading: true
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [loadingModel.packageId]: loadingModel
        }}));
    },

    [PKG_WIZARD_TYPES.PACKAGE_SUCCESS]: (state: PackageWizardContainer, action: any) => {
        const { model, response, view } = action;
        const { json } = response;
        const pkgId = model.packageId;

        let data = json.find((d) => {
            return d.pkgId = pkgId;
        });

        data.attributes = data.attributes.map((attribute, i) => {
            return Object.keys(attribute).reduce((prev, next) => {
                prev[next] = attribute[next];
                return prev;
            }, {});
        });


        const modelData = new PackageModel(Object.assign({}, state.packageData[pkgId].data, data));
        const successModel = new PackageWizardModel(Object.assign({}, model, {
            data: modelData,
            formView: view,
            initialData: modelData,
            isValid: isFormValid(modelData, modelData, view)
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
        }}));
    },

    [PKG_WIZARD_TYPES.SAVE_FIELD]: (state: PackageWizardContainer, action: any) => {
        const { model, name, value } = action;

        let data: PackageModel;

        if (name.indexOf('attributes') !== -1) {
            const parts = name.split('_');
            const index = parts[1];
            const attributeField = parts[2];

            let attributes = [].concat(model.data.attributes);
            attributes[index] = new PackageModelAttribute(
                Object.assign({}, model.data.attributes[index], {[attributeField]: value})
            );

            data = new PackageModel(Object.assign({}, model.data, {
                attributes
            }));
        }
        else {
            data = new PackageModel(Object.assign({}, model.data, {[name]: value}));
        }

        const updatedModel = new PackageWizardModel(Object.assign({}, model, {
            data,
            isValid: isFormValid(data, model.initialData, model.formView)
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [updatedModel.packageId]: updatedModel
        }}));
    },

    [PKG_WIZARD_TYPES.SAVE_NARRATIVE]: (state: PackageWizardContainer, action: any) => {
        const { model, narrative, parsedNarrative } = action;

        let data = Object.assign({}, model.data, {narrative});

        if (parsedNarrative.length !== model.data.attributes.length) {
            data.attributes = parsedNarrative.map((keyword, i) => {
                return new PackageModelAttribute(Object.assign({}, defaultPackageModelAttribute,
                    model.data.attributes[i],
                    {['name']: keyword}
                ));
            });
        }

        const successModel = new PackageWizardModel(Object.assign({}, model, {
            data: new PackageModel(Object.assign({}, state.packageData[model.packageId].data, data)),
            isValid: isFormValid(data, model.initialData, model.formView)
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
        }}));
    },

}, new PackageWizardContainer());

function isFormValid(data: PackageModel, initialData: PackageModel, view: PACKAGE_VIEW): boolean {

    let isValid: boolean = false;

    if (!data.description || !data.narrative) {
        return false;
    }

    if (data.attributes.length > 0) {
        isValid = data.attributes.every((attribute) => {
            return !!attribute.name && !!attribute.rangeURI;
        });
    }

    if (isValid && view === PACKAGE_VIEW.EDIT) {
        // need to loop through initialData to compare with currentValues if view === edit
        return (
            data.description !== initialData.description ||
            data.narrative !== initialData.narrative ||
            data.attributes.some((attribute, i) => {
                return Object.keys(attribute).findIndex((a) => {
                    if (initialData.attributes[i]) {
                        if (attribute[a] || initialData.attributes[i][a]) {
                            return attribute[a] !== initialData.attributes[i][a];
                        }
                        return false;
                    }
                    return false;
                }) !== -1;
            })
        )
    }

    return isValid;
}