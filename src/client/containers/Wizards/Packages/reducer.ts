import { handleActions } from 'redux-actions';

import { PKG_WIZARD_TYPES } from './constants'
import { defaultPackageWizardModel, PackageModel, PackageWizardModel, PackageWizardContainer } from './model'


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

        const data = json.reduce((prev, next: PackageModel) => {
            const id = next.pkgId;
            prev[id] = new PackageModel(next);

            return prev;
        }, {});


        const successModel = new PackageWizardModel(Object.assign({}, model, {
            data: Object.assign({}, state.packageData[pkgId].data, data)
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
        }}));
    },

}, new PackageWizardContainer());