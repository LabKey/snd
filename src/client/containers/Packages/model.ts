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
    message: string
    packageCount: number
}

export const defaultPackagesModel: PackagesModelProps = {
    active: [],
    data: {},
    dataIds: [],
    drafts: [],
    filteredActive: [],
    filteredDrafts: [],
    isError: false,
    isInit: false,
    message: undefined,
    packageCount: 0
};

// may make more sense for this to be in wizards/packages/packageSearch?
export class PackagesModel implements PackagesModelProps {
    active: Array<any>;
    data: {[key: string]: any};
    dataIds: Array<any>;
    drafts: Array<any>;
    filteredActive?: Array<any>;
    filteredDrafts?: Array<any>;
    isError: boolean;
    isInit?: boolean;
    message: string;
    packageCount: number;

    constructor(values: PackagesModelProps = defaultPackagesModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }

    init(data: QueryModel) {
        return actions.packagesInit(this, data);
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

export const defaultQueryPackageModel: QueryPackageModelProps = {
    Active: undefined,
    Container: undefined,
    Created: undefined,
    CreatedBy: undefined,
    Description: undefined,
    HasEvent: undefined,
    HasProject: undefined,
    ModifiedBy: undefined,
    Narrative: undefined,
    ObjectId: undefined,
    PkgId: undefined,
    QcState: undefined,
    Repeatable: undefined,
    links: undefined
};

export class QueryPackageModel implements QueryPackageModelProps {
    Active: LabKeyQueryRowPropertyProps;
    Container: LabKeyQueryRowPropertyProps;
    Created: LabKeyQueryRowPropertyProps;
    CreatedBy: LabKeyQueryRowPropertyProps;
    Description: LabKeyQueryRowPropertyProps;
    HasEvent: LabKeyQueryRowPropertyProps;
    HasProject: LabKeyQueryRowPropertyProps;
    ModifiedBy: LabKeyQueryRowPropertyProps;
    Narrative: LabKeyQueryRowPropertyProps;
    ObjectId: LabKeyQueryRowPropertyProps;
    PkgId: LabKeyQueryRowPropertyProps;
    QcState: LabKeyQueryRowPropertyProps;
    Repeatable: LabKeyQueryRowPropertyProps;
    links: any;

    constructor(values: QueryPackageModelProps = defaultQueryPackageModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}