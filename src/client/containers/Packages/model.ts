import { SND_PKG_QUERY, SND_PKG_SCHEMA } from './constants'
import { LabKeyQueryRowPropertyProps, QueryModel, SchemaQuery } from '../../query/model'

import * as actions from './actions'

export const schemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, SND_PKG_QUERY);

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
    showDrafts?: boolean
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
    packageCount: 0,
    showDrafts: false
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
    showDrafts?: boolean;

    constructor(values: PackagesModelProps = defaultPackagesModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
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