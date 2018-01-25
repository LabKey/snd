

import {ProjectModel, ProjectWizardContainer, ProjectWizardModel} from "./model";
import {PROJECT_WIZARD_TYPES} from "./constants";
import {handleActions} from "redux-actions";
import {PropertyDescriptor} from "../model";
import {AssignedPackageModel} from "../../SuperPackages/model";
import {getRevisionId} from "./actions";
import {VIEW_TYPES} from "../../App/constants";

export const projects = handleActions({


    [PROJECT_WIZARD_TYPES.PROJECT_ERROR]: (state: ProjectWizardContainer, action: any) => {
        const {error, model} = action;


        const errorModel = new ProjectWizardModel(Object.assign({}, model, {
            isError: true,
            message: error && error.exception ? error.exception : 'Something went wrong.'
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {
            projectData: {
                [getRevisionId(errorModel)]: errorModel
            }
        }));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_WARNING]: (state: ProjectWizardContainer, action: any) => {
        const {model, warning} = action;

        const warningModel = new ProjectWizardModel(Object.assign({}, model, {
            isWarning: !!warning, // if warning message is provided, set to true, otherwise false
            message: warning
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {
            projectData: {
                [getRevisionId(warningModel)]: warningModel
            }
        }));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_INIT]: (state: ProjectWizardContainer, action: any) => {
        const {idRev} = action.props;
        let id = -1, rev = 0;
        if (idRev !== -1) {
            let parts = idRev.split('|');
            id = parts[0];
            rev = parts[1];
        }

        const model = new ProjectWizardModel(
            Object.assign({}, state.projectData[idRev], {projectId: id}, {revisionNum: rev})
        );

        return new ProjectWizardContainer(Object.assign({}, state, {
            projectData: {
                [idRev]: model
            }
        }));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_INVALIDATE]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(model)]: new ProjectModel()
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_CHECK_VALID]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;
        const { data } = model;

        const successModel = new ProjectWizardModel(Object.assign({}, model, {
            isValid: isFormValid(data, data, model.formView)
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(successModel)]: successModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_LOADED]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new ProjectWizardModel(Object.assign({}, model, {
            projectLoaded: true,
            projectLoading: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(loadingModel)]: loadingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_LOADING]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new ProjectWizardModel(Object.assign({}, model, {
            projectLoaded: false,
            projectLoading: true
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(loadingModel)]: loadingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_SUCCESS]: (state: ProjectWizardContainer, action: any) => {
        const { model, response, view } = action;
        const { json } = response;
        const idRev = getRevisionId(model);

        let subPackages = [];

        if (json.projectItems && Array.isArray(json.projectItems)) {
            subPackages = json.projectItems.map(function (item) {
                return new AssignedPackageModel(item.superPkg.PkgId, item.superPkg.Description, item.superPkg.Narrative,
                    item.superPkg.Repeatable, item.superPkg.SuperPkgId, item.active, true, item.superPkg.SortOrder,
                    item.superPkg.SubPackages);
            });
        }

        let data = Object.assign({}, json, {subPackages: subPackages});

        const modelData = new ProjectModel(Object.assign({}, state.projectData[idRev].data, data));
        const successModel = new ProjectWizardModel(Object.assign({}, model, {
            data: modelData,
            formView: view,
            initialData: modelData,
            isValid: isFormValid(modelData, modelData, view)
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(successModel)]: successModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.SAVE_FIELD]: (state: ProjectWizardContainer, action: any) => {
        const { model, name, value } = action;

        let data: ProjectModel;

        if (name.indexOf('extraFields') !== -1) {
            const fieldParts = name.split('_');
            const fieldIndex = fieldParts[1];

            let extraFields = [].concat(model.data.extraFields);
            extraFields[fieldIndex] = new PropertyDescriptor(
                Object.assign({}, model.data.extraFields[fieldIndex], {["value"]: value})
            );

            data = new ProjectModel(Object.assign({}, model.data, {
                extraFields
            }));
        }
        else {
            data = new ProjectModel(Object.assign({}, model.data, {[name]: value}));
        }

        const updatedModel = new ProjectWizardModel(Object.assign({}, model, {
            data,
            isValid: isFormValid(data, model.initialData, model.formView)
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(updatedModel)]: updatedModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.SET_SUBMITTED]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const submittedModel = new ProjectWizardModel(Object.assign({}, model, {
            isSubmitted: true,
            isSubmitting: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(submittedModel)]: submittedModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.RESET_SUBMISSION]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const resetModel = new ProjectWizardModel(Object.assign({}, model, {
            isSubmitted: false,
            isSubmitting: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(resetModel)]: resetModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_FULL_NARRATIVE]: (state: ProjectWizardContainer, action: any) => {
        const { model, narrativePkg } = action;

        const submittingModel = new ProjectWizardModel(Object.assign({}, model, {
            narrativePkg: narrativePkg
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(submittingModel)]: submittingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_CLOSE_FULL_NARRATIVE]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const submittingModel = new ProjectWizardModel(Object.assign({}, model, {
            narrativePkg: null
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(submittingModel)]: submittingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.SET_REVISED_VALUES]: (state: ProjectWizardContainer, action: any) => {
        const { model, endDateRevised } = action;

        const data = new ProjectModel(Object.assign({}, model.data, {endDateRevised: endDateRevised}));
        const revisedModel = new ProjectWizardModel(Object.assign({}, model, {
            data
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [getRevisionId(revisedModel)]: revisedModel
        }}));
    }
}, new ProjectWizardContainer());

function isFormValid(data: ProjectModel, initialData: ProjectModel, view: VIEW_TYPES): boolean {

    let isValid: boolean = true;

    if (!data.description) {
        return false;
    }

    if (isValid && view === VIEW_TYPES.PROJECT_EDIT) {
        // need to loop through initialData to compare with currentValues if view === edit
        return (
            data.description !== initialData.description ||
            data.subPackages.map(p => p.PkgId).sort().join('') !==
            initialData.subPackages.map(p => p.PkgId).sort().join('')
        )
    }

    return isValid;
}