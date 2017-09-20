import { LabKeyQueryRowPropertyProps, QueryModel } from '../../query/model'

import * as actions from './actions'


interface PackagesModelProps {
    active: Array<any>
    data: {[key: string]: any}
    dataIds: Array<any>
    drafts: Array<any>
    filteredActive?: Array<any>
    filteredDrafts?: Array<any>
    isError: boolean
    isInit?: boolean
    isWarning?: boolean
    message: string
    packageCount: number
    showDrafts?: boolean
}

// may make more sense for this to be in wizards/packages/packageSearch?
export class PackagesModel implements PackagesModelProps {
    active: Array<any> = [];
    data: {[key: string]: any} = {};
    dataIds: Array<any> = [];
    drafts: Array<any> = [];
    filteredActive?: Array<any> = [];
    filteredDrafts?: Array<any> = [];
    isError: boolean = false;
    isInit?: boolean = false;
    isWarning?: boolean = false;
    message: string = undefined;
    packageCount: number = 0;
    showDrafts?: boolean = false;

    constructor(props?: Partial<PackagesModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }

    init(data: QueryModel) {
        return actions.packagesInit(this, data);
    }

    toggleDrafts() {
        return actions.toggleDrafts();
    }
}

interface QueryPackageModelProps {
    Active: LabKeyQueryRowPropertyProps
    Container: LabKeyQueryRowPropertyProps
    Created: LabKeyQueryRowPropertyProps
    CreatedBy: LabKeyQueryRowPropertyProps
    Description: LabKeyQueryRowPropertyProps
    HasEvent: LabKeyQueryRowPropertyProps
    HasProject: LabKeyQueryRowPropertyProps
    ModifiedBy: LabKeyQueryRowPropertyProps
    Narrative: LabKeyQueryRowPropertyProps
    ObjectId: LabKeyQueryRowPropertyProps
    PkgId: LabKeyQueryRowPropertyProps
    QcState: LabKeyQueryRowPropertyProps
    Repeatable: LabKeyQueryRowPropertyProps
    links: any
}

export class QueryPackageModel implements QueryPackageModelProps {
    Active: LabKeyQueryRowPropertyProps = undefined;
    Container: LabKeyQueryRowPropertyProps = undefined;
    Created: LabKeyQueryRowPropertyProps = undefined;
    CreatedBy: LabKeyQueryRowPropertyProps = undefined;
    Description: LabKeyQueryRowPropertyProps = undefined;
    HasEvent: LabKeyQueryRowPropertyProps = undefined;
    HasProject: LabKeyQueryRowPropertyProps = undefined;
    ModifiedBy: LabKeyQueryRowPropertyProps = undefined;
    Narrative: LabKeyQueryRowPropertyProps = undefined;
    ObjectId: LabKeyQueryRowPropertyProps = undefined;
    PkgId: LabKeyQueryRowPropertyProps = undefined;
    QcState: LabKeyQueryRowPropertyProps = undefined;
    Repeatable: LabKeyQueryRowPropertyProps = undefined;
    links: any = undefined;

    constructor(props?: Partial<QueryPackageModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface QuerySuperPackageModelProps {
    Container: LabKeyQueryRowPropertyProps
    Created: LabKeyQueryRowPropertyProps
    CreatedBy: LabKeyQueryRowPropertyProps
    Modified: LabKeyQueryRowPropertyProps
    ModifiedBy: LabKeyQueryRowPropertyProps
    ParentSuperPkgId: LabKeyQueryRowPropertyProps
    SuperPkgPath: LabKeyQueryRowPropertyProps
    SuperPkgId: LabKeyQueryRowPropertyProps
    PkgId: LabKeyQueryRowPropertyProps
    IsPrimitive: LabKeyQueryRowPropertyProps
    Narrative: LabKeyQueryRowPropertyProps
    links: any
}

export class QuerySuperPackageModel implements QuerySuperPackageModelProps {
    Container: LabKeyQueryRowPropertyProps = undefined;
    Created: LabKeyQueryRowPropertyProps = undefined;
    CreatedBy: LabKeyQueryRowPropertyProps = undefined;
    IsPrimitive: LabKeyQueryRowPropertyProps = undefined;
    Modified: LabKeyQueryRowPropertyProps = undefined;
    ModifiedBy: LabKeyQueryRowPropertyProps = undefined;
    Narrative: LabKeyQueryRowPropertyProps = undefined;
    ParentSuperPkgId: LabKeyQueryRowPropertyProps = undefined;
    SuperPkgPath: LabKeyQueryRowPropertyProps = undefined;
    SuperPkgId: LabKeyQueryRowPropertyProps = undefined;
    PkgId: LabKeyQueryRowPropertyProps = undefined;
    links: any = undefined;

    constructor(props?: Partial<QuerySuperPackageModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface AssignedPackageModelProps {
    SuperPkgId: number
    PkgId: number
    Description: string
    Narrative: string
    altId: number
}

export class AssignedPackageModel implements AssignedPackageModelProps {
    SuperPkgId: number = undefined;
    PkgId: number = undefined;
    Description: string = undefined;
    Narrative: string = null;

    // set the altId as a way to uniquely remove this assigned package or to handle assigned package click
    altId: number = LABKEY.Utils.id();

    constructor(pkgId: number, description: string, narrative: string, superPkgId?: number) {
        this.PkgId = pkgId;
        this.Description = description;
        this.Narrative = narrative;
        this.SuperPkgId = superPkgId;
    }
}