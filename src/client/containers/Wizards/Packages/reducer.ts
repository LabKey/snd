import { handleActions } from 'redux-actions';

import {PKG_WIZARD_TYPES, VALIDATOR_LTE, VALIDATOR_GTE} from './constants'
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
        const { error, model } = action;


        const errorModel = new PackageWizardModel(Object.assign({}, model, {
            isError: true,
            message: error && error.exception ? error.exception : 'Something went wrong.'
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [errorModel.packageId]: errorModel
        }}));
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

    [PKG_WIZARD_TYPES.PACKAGE_INVALIDATE]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [model.packageId]: new PackageModel()
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

        let sourceKeywords = {};

        data.attributes = data.attributes.map((attribute, i) => {
            let attributeValues = Object.assign({}, attribute, {
                rangeURI: attribute.rangeURI ? attribute.rangeURI : 'string',
                sortOrder: attribute.sortOrder || attribute.sortOrder === 0 ? attribute.sortOrder : i
            });
            if (attribute["lookupSchema"] && attribute["lookupQuery"]) {
                attributeValues["lookupKey"] = [attribute["lookupSchema"], attribute["lookupQuery"]].join('.');
            }

            // Set up min/max values from validator expression
            if (attribute.validators[0] && attribute.validators[0].expression) {

                // Only display SND created validators
                if (attribute.validators[0].name && attribute.validators[0].name.startsWith("SND")) {
                    let minRegEx = new RegExp(VALIDATOR_GTE + "([0-9]+)");
                    let maxRegEx = new RegExp(VALIDATOR_LTE + "([0-9]+)");
                    let min = attribute.validators[0].expression.match(minRegEx);
                    let max = attribute.validators[0].expression.match(maxRegEx);
                    attributeValues.min = (min != null ? min[1] : undefined);
                    attributeValues.max = (max != null ? max[1] : undefined);
                }
            }

            sourceKeywords[attributeValues.name] = attributeValues.sortOrder;

            return new PackageModelAttribute(attributeValues);
        }).sort((attA, attB) => {
            const a = attA.sortOrder,
                b = attB.sortOrder;
            return a < b ? -1 : a == b ? 0 : 1;
        });

        const keywords = parseNarrativeKeywords(data.narrative);
        if (!arraysMatch(keywords, Object.keys(sourceKeywords))) {
            console.warn('Server assigned attributes do not match parsed values', keywords, Object.keys(sourceKeywords));
            // if the server attribute names do not match the parsed narrative
            data.narrativeKeywords = keywords;
        }
        else {
            data.narrativeKeywords = sourceKeywords;
        }

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


    [PKG_WIZARD_TYPES.PARSE_ATTRIBUTES]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const narrative = model.data.narrative;
        const parsedKeywords = parseNarrativeKeywords(narrative);

        let data = Object.assign({}, model.data);

        // compare the parsed and existing keyword arrays to see if they match
        if (!arraysMatch(parsedKeywords, Object.keys(model.data.narrativeKeywords))) {

            let changedIndex = parsedKeywords.findIndex((e) => {
                return model.data.narrativeKeywords[e] === undefined;
            });

            // create a new object to store the narrative keywords and their index
            const narrativeKeywords = parsedKeywords.reduce((prev, next, currentIndex) => {
                let existingIndex = model.data.narrativeKeywords[next];
                let index = existingIndex !== undefined ? existingIndex : currentIndex;

                // if the attribute key already existed, make sure we are inserting it into the correct
                // place in the array
                if (
                    (existingIndex && existingIndex > changedIndex) ||
                    (currentIndex === changedIndex && changedIndex !== (parsedKeywords.length - 1))
                ) {
                    index++;
                }

                prev[next] = index;

                return prev;
            }, {});

            data.narrativeKeywords = narrativeKeywords;

            data.attributes = Object.keys(narrativeKeywords).map(keyword => {
                const existingIndex = model.data.narrativeKeywords[keyword];
                return new PackageModelAttribute(Object.assign({},
                    model.data.attributes[existingIndex],
                    {
                        name: keyword,
                        sortOrder: narrativeKeywords[keyword]
                    }
                ));
            }).sort((a,b) => {
                return a.sortOrder < b.sortOrder ? -1 : a.sortOrder === b.sortOrder ? 0 : 1;
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

    [PKG_WIZARD_TYPES.SAVE_FIELD]: (state: PackageWizardContainer, action: any) => {
        const { model, name, value } = action;

        let data: PackageModel;

        if (name.indexOf('attributes') !== -1) {
            const parts = name.split('_');
            const index = parts[1];
            const attributeField = parts[2];
            let attributeValue = value;
            let attributes = [].concat(model.data.attributes);
            let narrativeKeywords = model.data.narrativeKeywords;
            if (attributeField === 'sortOrder') {
                const prevValue = model.data.attributes[index].sortOrder,
                    attributeName = model.data.attributes[index].name;
                attributeValue = value === 'up' ? prevValue - 1 : prevValue + 1;
                const prevName = model.data.attributes[attributeValue].name;

                // move the existing attribute to replace the changed attribute
                attributes[attributeValue] = new PackageModelAttribute(
                    Object.assign({}, model.data.attributes[attributeValue], {['sortOrder']: prevValue})
                );

                // ensure the index for the keywords is updated as well if sort order has changed
                narrativeKeywords = Object.assign({},
                    model.data.narrativeKeywords,
                    {
                        [attributeName]: attributeValue,
                        [prevName]: prevValue,
                    }
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
                }),
                narrativeKeywords
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

    [PKG_WIZARD_TYPES.PACKAGE_FULL_NARRATIVE]: (state: PackageWizardContainer, action: any) => {
        const { model, narrativePkg } = action;

        const submittingModel = new PackageWizardModel(Object.assign({}, model, {
            narrativePkg: narrativePkg
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [submittingModel.packageId]: submittingModel
        }}));
    },

    [PKG_WIZARD_TYPES.PACKAGE_CLOSE_FULL_NARRATIVE]: (state: PackageWizardContainer, action: any) => {
        const { model } = action;

        const submittingModel = new PackageWizardModel(Object.assign({}, model, {
            narrativePkg: null
        }));

        return new PackageWizardContainer(Object.assign({}, state, {packageData: {
            [submittingModel.packageId]: submittingModel
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
            }) || // check list of initial and current subpackages, if changed, form is valid to save
            data.subPackages.map(p => p.PkgId).sort().join('') !==
            initialData.subPackages.map(p => p.PkgId).sort().join('')
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
