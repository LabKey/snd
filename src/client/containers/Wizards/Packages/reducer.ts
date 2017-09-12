import { handleActions } from 'redux-actions';
import { reducer as formReducer } from 'redux-form'

import { PKG_WIZARD_TYPES } from './constants'
import {
    defaultPackageWizardModel,
    PackageModelAttribute,
    PackageModel,
    PackageWizardModel,
    PackageWizardContainer
} from './model'

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
        const { model, response } = action;
        const { json } = response;
        const pkgId = model.packageId;

        let data = json.find((d) => {
            return d.pkgId = pkgId;
        });

        data.attributes = data.attributes.map((attribute, i) => {
            return Object.keys(attribute).reduce((prev, next) => {
                prev[next + i] = attribute[next];
                return prev;
            }, {});
        });


        const successModel = new PackageWizardModel(Object.assign({}, model, {
            data: new PackageModel(Object.assign({}, state.packageData[pkgId].data, data))
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
        }}));
    },

    [PKG_WIZARD_TYPES.SAVE_NARRATIVE]: (state: PackageWizardContainer, action: any) => {
        const { model, narrative, parsedNarrative } = action;

        let data = Object.assign({}, model.data, {narrative});

        data.attributes = parsedNarrative.map((keyword, i) => {
            return new PackageModelAttribute({['name_' + i]: keyword});
        });

        const successModel = new PackageWizardModel(Object.assign({}, model, {
            data: new PackageModel(Object.assign({}, state.packageData[model.packageId].data, data))
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
        }}));
    },

}, new PackageWizardContainer());

const formReducerMiddleware = formReducer as any;
export const packageFormReducerPlugin = formReducerMiddleware.plugin({
    packageForm: (state, action) => {
        switch(action.type) {

            case PKG_WIZARD_TYPES.SAVE_NARRATIVE:

                const currentAttributes = action.model.data.attributes;
                const updatedAttributes = action.parsedNarrative;

                if (currentAttributes.length !== updatedAttributes.length) {

                    let attributes = updatedAttributes.reduce((prev, next, i) => {
                        prev[i] = Object.assign({}, state.values.attributes[i], {
                            ['name_' + i]: updatedAttributes[i]
                        });
                        return prev;
                    }, {});

                    return {
                        ...state,
                        values: {
                            ...state.values,
                            attributes // set attribute value based on newly updated narrative
                        },
                    };
                }

                return state;
            default:
                return state;
        }
    }
});