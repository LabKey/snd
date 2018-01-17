
import { labkeyAjax, queryInvalidate } from '../../../query/actions';
import { PROJECT_VIEW } from '../../Projects/Forms/ProjectFormContainer';
import {PROJECT_WIZARD_TYPES} from './constants';
import {
    ProjectModel, ProjectQueryResponse, ProjectWizardModel, ProjectSubmissionModel
} from './model';
import { projectsInvalidate } from '../../Projects/actions';
import { PROJECT_SQL } from '../../Projects/constants'
import {push} from "react-router-redux";
import {setAppError} from "../../App/actions";


function fetchProject(idRev: string | number)
{
    let id, rev;

    if (idRev === -1)
    {
        id = -1;
        rev = 0;
    }
    else if (typeof idRev === 'string') {
        id = idRev.split('|')[0];
        rev = idRev.split('|')[1];
    }

    return labkeyAjax(
        'snd',
        'getProject',
        null,
        {
            'projectId': id,
            'revisionNum': rev
        }
    );
}

function saveProject(jsonData) {
    return new Promise((resolve, reject) => {
        LABKEY.Ajax.request({
            method: 'POST',
            url: LABKEY.ActionURL.buildURL('snd', 'saveProject.api'),
            jsonData,
            success: LABKEY.Utils.getCallbackWrapper((data) => {
                resolve(data);
            }),
            failure: LABKEY.Utils.getCallbackWrapper((data) => {
                reject(data);
            })
        });
    })
}

function getProjectModelFromResponse(response: ProjectQueryResponse): ProjectModel {
    // the response should have exactly one row
    return Array.isArray(response.json) && response.json.length == 1 ?
        response.json[0] :
        new ProjectModel();
}

export function init(idRev: string | number, view: PROJECT_VIEW) {
    if (!idRev)
        idRev = -1;

    return (dispatch, getState) => {
        let projectModel: ProjectWizardModel = getState().wizards.projects.projectData[idRev];

        // todo: make this cleaner - works for now
        if (!projectModel || projectModel.formView !== view) {
            const projectModelProps = {
                idRev: idRev,
                formView: view
            };
            dispatch(initProjectModel(projectModelProps));


            const model = getState().wizards.projects.projectData[idRev];
            if (projectModel && projectModel.formView !== view && view !== PROJECT_VIEW.VIEW) {
                dispatch(model.checkValid());
            }
        }

        projectModel = getState().wizards.projects.projectData[idRev];

        if (shouldFetch(projectModel)) {
            dispatch(projectModel.loading());

            return fetchProject(idRev).then((response: ProjectQueryResponse) => {

                projectModel = getState().wizards.projects.projectData[idRev];
                dispatch(projectModel.success(response, view));

                projectModel = getState().wizards.projects.projectData[idRev];
                dispatch(projectModel.loaded());
            }).catch((error) => {
                // set error
                console.log('error', error);
                dispatch(projectModel.setError(error));
            });
        }

        else if (projectModel && !projectModel.projectLoaded && !projectModel.projectLoading) {
            dispatch(projectModel.loaded());
        }
    }
}

export function initProjectModel(props: {[key: string]: any}) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_INIT,
        props
    }
}

function shouldFetch(model: ProjectWizardModel): boolean {
    return !model.projectLoaded && !model.projectLoading && !model.isError;
}

export function projectCheckValid(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_CHECK_VALID,
        model
    }
}

export function projectError(model: ProjectWizardModel, error: any) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_ERROR,
        error,
        model
    }
}

export function projectLoaded(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_LOADED,
        model
    }
}

export function projectLoading(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_LOADING,
        model
    };
}

export function projectSuccess(model: ProjectWizardModel, response: ProjectQueryResponse, view: PROJECT_VIEW) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_SUCCESS,
        model,
        response,
        view
    };
}

export function projectWarning(model: ProjectWizardModel, warning?: string) {
    return {
        type: PROJECT_WIZARD_TYPES.PROJECT_WARNING,
        model,
        warning
    }
}

// export function saveNarrative(model: ProjectWizardModel, narrative: string) {
//     return (dispatch) => {
//         dispatch({
//             type: PROJECT_WIZARD_TYPES.SAVE_NARRATIVE,
//             model,
//             narrative
//         });
//     }
// }

export function saveField(model: ProjectWizardModel, name: string, value: any) {
    return {
        type: PROJECT_WIZARD_TYPES.SAVE_FIELD,
        model,
        name,
        value
    };
}


export function resetSubmitting(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.RESET_SUBMISSION,
        model
    };
}

export function setSubmitted(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.SET_SUBMITTED,
        model
    };
}

export function setSubmitting(model: ProjectWizardModel) {
    return {
        type: PROJECT_WIZARD_TYPES.SET_SUBMITTING,
        model
    };
}

export function save(model: ProjectWizardModel, project: ProjectSubmissionModel) {
    return (dispatch, getState) => {
        dispatch(setSubmitting(model));
        const updatedModel = getState().wizards.projects.projectData[model.projectId];

        return saveProject(project).then((response) => {
            dispatch(setSubmitted(updatedModel));
            dispatch(projectsInvalidate());
            dispatch(queryInvalidate(PROJECT_SQL));
            //onSuccess('/packages');
            dispatch(push('/projects'));
        }).catch((error) => {
            console.warn('save project error', error);
            dispatch(resetSubmitting(updatedModel));
            dispatch(setAppError(error));
        });
    }
}

// export function formatProjectValues(model: ProjectWizardModel, active: boolean): ProjectSubmissionModel {
//     const { formView } = model;
//     const { description, extraFields, projectId, revisionNum, objectId } = model.data;
//     let id, rev, obj;
//     if (formView !== PROJECT_VIEW.NEW) {
//         id = projectId;
//         rev = revisionNum;
//         obj = objectId;
//     }
//
//     // const subPackages = formatSubPackages(model.data.subPackages);
//
//     return new ProjectSubmissionModel({
//         active,
//         description,
//         extraFields,
//         projectId,
//         revisionNum
//         // startDate,
//         // subPackages
//     });
// }