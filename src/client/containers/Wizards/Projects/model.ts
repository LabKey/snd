
import * as actions from './actions';
import {PropertyDescriptor} from "../model";
import { PROJECT_VIEW } from '../../Projects/Forms/ProjectFormContainer';
import {AssignedPackageModel} from "../../SuperPackages/model";

interface ProjectSubmissionModelProps {
    active: boolean;
    isRevision?: boolean;
    isEdit?: boolean;
    description: string;
    extraFields?: Array<PropertyDescriptor>;
    projectId: number;
    revisionNum: number;
    startDate: string;
    endDate: string;
    referenceId: number;
    qcState?: any;
    subPackages?: Array<{sortOrder: number, superPkgId: number}>;
}

export class ProjectSubmissionModel implements ProjectSubmissionModelProps {
    active: boolean = false;
    isRevision: boolean = false;
    isEdit: boolean = false;
    description: string = undefined;
    extraFields: Array<PropertyDescriptor> = [];
    projectId: number = undefined;
    revisionNum: number = undefined;
    startDate: string = undefined;
    endDate: string = undefined;
    referenceId: number = undefined;
    qcState: any = null;
    subPackages: Array<{sortOrder: number, superPkgId: number}> = [];

    constructor(props: Partial<ProjectSubmissionModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface ProjectQueryResponseProps {
    json: ProjectModel
}

export class ProjectQueryResponse {
    json: ProjectModel = undefined;

    constructor(props?: Partial<ProjectModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface ProjectModelProps {
    active?: boolean
    container?: string
    description?: string
    extraFields?: Array<PropertyDescriptor>
    hasEvent?: boolean
    projectId?: number
    revisionNum?: number
    startDate?: string
    endDate?: string
    objectId?: string
    referenceId?: string
    qcState?: any
    subPackages?: Array<AssignedPackageModel>
}

export class ProjectModel implements ProjectModelProps {
    active: boolean = false;
    container: string = undefined;
    description: string = undefined;
    extraFields: Array<PropertyDescriptor> = [];
    hasEvent: boolean = false;
    projectId: number = undefined;
    revisionNum: number = undefined;
    startDate: string = undefined;
    endDate: string = undefined;
    objectId: string = undefined;
    referenceId: string = undefined
    qcState: any = null; // todo: find out qcState type
    subPackages: Array<AssignedPackageModel> = [];

    constructor(props?: Partial<ProjectModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface ProjectWizardModelProps {
    data?: ProjectModel;
    formView?: PROJECT_VIEW;
    initialData?: ProjectModel;
    isActive?: boolean;
    isError?: boolean;
    isSubmitted?: boolean;
    isSubmitting?: boolean;
    isValid?: boolean;
    isWarning?: boolean;
    message?: string;
    // narrativePkg?: AssignedPackageModel;
    // packageCount?: number;
    projectId?: number
    revisionNum?: number
    objectId?: number
    projectLoaded?: boolean;
    projectLoading?: boolean;
}

export class ProjectWizardModel implements ProjectWizardModelProps {
    data: ProjectModel = new ProjectModel();
    formView: PROJECT_VIEW = undefined;
    initialData: ProjectModel = new ProjectModel();
    isActive: boolean = false;
    isError: boolean = false;
    isSubmitted: boolean = false;
    isSubmitting: boolean = false;
    isValid: boolean = false;
    isWarning: boolean = false;
    message: string = undefined;
    // narrativePkg?: AssignedPackageModel = undefined;
    // packageCount: number = 0;
    objectId: number = undefined;
    projectId: number = undefined;
    revisionNum: number = undefined;
    projectLoaded: boolean = false;
    projectLoading: boolean = false;

    constructor(props: Partial<ProjectWizardModelProps>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }

    getIdRev() {
        if (typeof this.projectId === 'undefined') {
            return -1;
        }
        else {
            return this.projectId + '|' + this.revisionNum;
        }
    }

    // checkValid() {
    //     return actions.packageCheckValid(this);
    // }

    // formatPackageValues(active: boolean): PackageSubmissionModel {
    //     return actions.formatPackageValues(this, active);
    // }

    // invalidate() {
    //     return actions.invalidateModel(this);
    // }

    loaded() {
        return actions.projectLoaded(this);
    }

    loading() {
        return actions.projectLoading(this);
    }

    saveField(name, value) {
        return actions.saveField(this, name, value);
    }

    setError(error) {
        return actions.projectError(this, error);
    }

    setWarning(warning?: string) {
        return actions.projectWarning(this, warning);
    }

    // submitForm(active: boolean) {
    //     return actions.save(this, this.formatPackageValues(active));
    // }
    //
    success(response: ProjectQueryResponse, view: PROJECT_VIEW) {
        return actions.projectSuccess(this, response, view);
    }
}

interface ProjectWizardContainerProps {
    projectData: {[key: string]: ProjectWizardModel}
}

export class ProjectWizardContainer implements ProjectWizardContainerProps {
    projectData: {[key: string]: ProjectWizardModel} = {};

    constructor(props?: Partial<ProjectWizardContainer>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}