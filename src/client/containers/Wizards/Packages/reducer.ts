import { handleActions } from 'redux-actions';

import { PKG_WIZARD_TYPES } from './constants'
import {
    PackageModelAttribute,
    PackageModel,
    PackageWizardModel,
    PackageWizardContainer
} from './model'
import { PACKAGE_VIEW } from "../../Packages/Forms/PackageFormContainer";
import { arraysMatch } from '../../../utils/actions'

export const packages = handleActions({

    [PKG_WIZARD_TYPES.PACKAGE_ERROR]: (state: PackageWizardContainer, action: any) => {
        //const { model } = action;

        return state;
    },

    [PKG_WIZARD_TYPES.PACKAGE_INIT]: (state: PackageWizardContainer, action: any) => {
        const { packageId } = action.props;

        const model = new PackageWizardModel(
            Object.assign({}, state.packageData[packageId], {packageId, ...action.props})
        );

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [packageId]: model
        }}));
    },

    [PKG_WIZARD_TYPES.PACKAGE_CHECK_VALID]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;
        const { data } = model;

        const successModel = new PackageWizardModel(Object.assign({}, model, {
            isValid: isFormValid(data, data, model.formView)
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [successModel.packageId]: successModel
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
            if (attribute["lookupSchema"] && attribute["lookupQuery"])
                attribute["lookupKey"] = attribute["lookupSchema"] + "." + attribute["lookupQuery"];
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
        else if (name.indexOf('extraFields') !== -1) {
            const fieldParts = name.split('_');
            const fieldIndex = fieldParts[1];

            let extraFields = [].concat(model.data.extraFields);
            extraFields[fieldIndex] = new PackageModelAttribute(
                Object.assign({}, model.data.extraFields[fieldIndex], {["value"]: value})
            );

            data = new PackageModel(Object.assign({}, model.data, {
                extraFields
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
        const { model, narrative } = action;

        let data = Object.assign({}, model.data, {narrative});
        const narrativeKeywords = parseNarrativeKeywords(narrative);

        // compare the parsed and existing keyword arrays to see if they match
        if (!arraysMatch(narrativeKeywords, model.data.narrativeKeywords)) {

            data.narrativeKeywords = narrativeKeywords;

            // look for more efficient method to set attributes/keywords
            data.attributes = narrativeKeywords.map((keyword, i) => {
                return new PackageModelAttribute(Object.assign({},
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

    let isValid: boolean = true;

    if (!data.description || !data.narrative) {
        return false;
    }

    if (data.attributes.length > 0) {
        isValid = data.attributes.every((attribute) => {
            return !!attribute.name && !!attribute.rangeURI;
        });
    }

    // add check for updated categories

    //todo: add check for sub/superpackages
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
