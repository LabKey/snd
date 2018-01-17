

import {ProjectModel, ProjectWizardContainer, ProjectWizardModel} from "./model";
import {PROJECT_WIZARD_TYPES} from "./constants";
import {handleActions} from "redux-actions";
import {PROJECT_VIEW} from "../../Projects/Forms/ProjectFormContainer";

export const projects = handleActions({


    [PROJECT_WIZARD_TYPES.PROJECT_ERROR]: (state: ProjectWizardContainer, action: any) => {
        const {error, model} = action;


        const errorModel = new ProjectWizardModel(Object.assign({}, model, {
            isError: true,
            message: error && error.exception ? error.exception : 'Something went wrong.'
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {
            projectData: {
                [errorModel.getIdRev()]: errorModel
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
                [warningModel.getIdRev()]: warningModel
            }
        }));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_INIT]: (state: ProjectWizardContainer, action: any) => {
        const {idRev} = action.props;
        let parts = idRev.split('|');
        let id = parts[0];
        let rev = parts[1];

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
            [model.getIdRev()]: new ProjectModel()
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_CHECK_VALID]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;
        const { data } = model;

        const successModel = new ProjectWizardModel(Object.assign({}, model, {
            isValid: isFormValid(data, data, model.formView)
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [successModel.getIdRev()]: successModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_LOADED]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new ProjectWizardModel(Object.assign({}, model, {
            projectLoaded: true,
            projectLoading: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [loadingModel.getIdRev()]: loadingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_LOADING]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const loadingModel = new ProjectWizardModel(Object.assign({}, model, {
            projectLoaded: false,
            projectLoading: true
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [loadingModel.getIdRev()]: loadingModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.PROJECT_SUCCESS]: (state: ProjectWizardContainer, action: any) => {
        const { model, response, view } = action;
        const { json } = response;
        const idRev = model.getIdRev();

        let data = json;

        let sourceKeywords = {};

        const modelData = new ProjectModel(Object.assign({}, state.projectData[idRev].data, data));
        const successModel = new ProjectWizardModel(Object.assign({}, model, {
            data: modelData,
            formView: view,
            initialData: modelData,
            isValid: isFormValid(modelData, modelData, view)
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [successModel.getIdRev()]: successModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.SET_SUBMITTED]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const submittedModel = new ProjectWizardModel(Object.assign({}, model, {
            isSubmitted: true,
            isSubmitting: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [submittedModel.getIdRev()]: submittedModel
        }}));
    },

    [PROJECT_WIZARD_TYPES.RESET_SUBMISSION]: (state: ProjectWizardContainer, action: any) => {
        const { model } = action;

        const resetModel = new ProjectWizardModel(Object.assign({}, model, {
            isSubmitted: false,
            isSubmitting: false
        }));

        return new ProjectWizardContainer(Object.assign({}, state, {projectData: {
            [resetModel.getIdRev()]: resetModel
        }}));
    }
}, new ProjectWizardContainer());

function isFormValid(data: ProjectModel, initialData: ProjectModel, view: PROJECT_VIEW): boolean {

    let isValid: boolean = true;

    if (!data.description) {
        return false;
    }

    if (isValid && view === PROJECT_VIEW.EDIT) {
        // need to loop through initialData to compare with currentValues if view === edit
        return (
            data.description !== initialData.description
            // data.subPackages.map(p => p.PkgId).sort().join('') !==
            // initialData.subPackages.map(p => p.PkgId).sort().join('')
        )
    }

    return isValid;
}