
import { handleActions } from 'redux-actions';

import { PROJECT_TYPES } from './constants'
import { QueryProjectModel, ProjectsModel } from './model'

export const projects = handleActions({

    [PROJECT_TYPES.PROJECTS_INIT]: (state: ProjectsModel, action: any) => {
        const { dataResponse } = action;
        const { data, dataCount, dataIds } = dataResponse;

        let active = [],
            ids = [],
            drafts = [];
        const projectsData = dataIds.reduce((prev, next: number) => {
            const projectData = data[next];
            const id = [projectData.ProjectId.value, projectData.RevisionNum.value].join('|');
            ids.push(id);
            prev[id] = new QueryProjectModel(projectData);
            // // should filter on hasEvent or Active?
            if (projectData.Active.value === true) {
                active.push(id);
            }
            else {
                drafts.push(id);
            }

            return prev;
        }, {});

        return new ProjectsModel(Object.assign({}, state, {
            active,
            data: projectsData,
            dataIds: ids,
            drafts,
            isInit: true,
            filteredActive: active,
            filteredDrafts: drafts,
            projectCount: dataCount
        }));
    },

    [PROJECT_TYPES.PROJECTS_INVALIDATE]: () => {
        return new ProjectsModel();
    },

    [PROJECT_TYPES.PROJECTS_RESET_FILTER]: (state: ProjectsModel) => {
        const { active, drafts } = state;

        return new ProjectsModel(Object.assign({}, state, {
            filteredActive: active,
            filteredDrafts: drafts
        }));
    },

    [PROJECT_TYPES.PROJECTS_RESET_WARNING]: (state: ProjectsModel) => {
        return new ProjectsModel(Object.assign({}, state, {
            isWarning: false,
            message: undefined
        }));
    },

    [PROJECT_TYPES.PROJECTS_SEARCH_FILTER]: (state: ProjectsModel, action: any) => {
        const { active, data, dataIds, drafts } = state;
        const { input } = action;

        let filteredActive = active,
            filteredDrafts = drafts;

        if (input && input !== '') {
            const filtered = filterProjects(input, dataIds, data);
            filteredActive = filtered.filter((id) => {
                return active.indexOf(id) !== -1;
            });

            filteredDrafts = filtered.filter((id) => {
                return drafts.indexOf(id) !== -1;
            });
        }

        return new ProjectsModel(Object.assign({}, state, {
            filteredActive,
            filteredDrafts,
            input
        }));
    },

    [PROJECT_TYPES.PROJECTS_TOGGLE_DRAFTS]: (state: ProjectsModel, action: any) => {
        const { toggled } = action;

        const showDrafts = toggled && typeof toggled === 'boolean' ? toggled : !state.showDrafts;

        return new ProjectsModel(Object.assign({}, state, {
            showDrafts
        }));
    },

    [PROJECT_TYPES.PROJECTS_WARNING]: (state: ProjectsModel, action: any) => {
        const { message } = action;

        return new ProjectsModel(Object.assign({}, state, {
            isWarning: true,
            message
        }));
    },

}, new ProjectsModel());

function filterProjects(input: string, dataIds: Array<number> , data: {[key: string]: any}) {

    return dataIds.filter((id: number) => {
        const project: QueryProjectModel = data[id];

        if (project) {
            return (
                project.Description &&
                project.Description.value.toLowerCase().indexOf(input.toLowerCase()) !== -1
            ) || (
                project.ProjectId &&
                project.ProjectId.value.toString().indexOf(input) !== -1
            )
        }

        return false;
    });

}