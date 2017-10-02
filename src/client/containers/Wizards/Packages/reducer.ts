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

    [PKG_WIZARD_TYPES.PACKAGE_WARNING]: (state: PackageWizardContainer, action: any) => {
        const { model, warning } = action;

        const warningModel = new PackageWizardModel(Object.assign({}, model, {
            isWarning: !!warning, // if warning message is provided, set to true, otherwise false
            message: warning
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [warningModel.packageId]: warningModel
        }}));
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
            let attributeValues = Object.assign({}, attribute, {
                rangeURI: attribute.rangeURI ? attribute.rangeURI : 'string',
                sortOrder: attribute.sortOrder || attribute.sortOrder === 0 ? attribute.sortOrder : i
            });
            if (attribute["lookupSchema"] && attribute["lookupQuery"]) {
                attributeValues["lookupKey"] = [attribute["lookupSchema"], attribute["lookupQuery"]].join('.');
            }

            return new PackageModelAttribute(attributeValues);
        }).sort((attA, attB) => {
            const a = attA.sortOrder,
                b = attB.sortOrder;
            return a < b ? -1 : a == b ? 0 : 1;
        });

        data.narrativeKeywords = parseNarrativeKeywords(data.narrative);

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
            let attributeValue = value;
            let attributes = [].concat(model.data.attributes);

            if (attributeField === 'sortOrder') {
                const prevValue = model.data.attributes[index].sortOrder;
                attributeValue = value === 'up' ? prevValue - 1 : prevValue + 1;

                // move the existing attribute to replace the changed attribute
                attributes[attributeValue] = new PackageModelAttribute(
                    Object.assign({}, model.data.attributes[attributeValue], {['sortOrder']: prevValue})
                );
            }

            attributes[index] = new PackageModelAttribute(
                Object.assign({}, model.data.attributes[index], {[attributeField]: attributeValue})
            );

            data = new PackageModel(Object.assign({}, model.data, {
                attributes: attributes.sort((attA, attB) => {
                    const a = attA.sortOrder,
                        b = attB.sortOrder;
                    return a < b ? -1 : a == b ? 0 : 1;
                })
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
            // sort order is set based on order or keywords, but if a different order has been set, the original will
            // override
            data.attributes = narrativeKeywords.map((keyword, i) => {
                return new PackageModelAttribute(Object.assign({},
                    {
                        name: keyword,
                        sortOrder: i
                    },
                    model.data.attributes[i]
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

    [PKG_WIZARD_TYPES.SET_SUBMITTED]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const submittedModel = new PackageWizardModel(Object.assign({}, model, {
            isSubmitted: true,
            isSubmitting: false
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [submittedModel.packageId]: submittedModel
        }}));
    },

    [PKG_WIZARD_TYPES.RESET_SUBMISSION]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const resetModel = new PackageWizardModel(Object.assign({}, model, {
            isSubmitted: false,
            isSubmitting: false
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [resetModel.packageId]: resetModel
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

    //todo: add check for sub/superpackages
    if (isValid && view === PACKAGE_VIEW.EDIT) {
        // need to loop through initialData to compare with currentValues if view === edit
        return (
            data.description !== initialData.description ||
            data.narrative !== initialData.narrative ||
            data.repeatable !== initialData.repeatable ||
            data.categories.sort().join('') !== initialData.categories.sort().join('') ||
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
    if (narrative) {
        let start,
            keyword = [],
            keywords = [];

        for (let char of narrative) {
            if (!start && char === '{') {
                start = true;
            }
            else if (start && char === '}' && keyword.length) {
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
    }

    return [];
}
