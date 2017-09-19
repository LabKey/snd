import {SND_PKG_QUERY, SND_PKG_SCHEMA, SND_CATEGORY_QUERY, SND_SUPER_PKG_QUERY} from './constants'
import { LabKeyQueryRowPropertyProps, QueryModel, SchemaQuery } from '../../query/model'

import * as actions from './actions'

export const pkgSchemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, SND_PKG_QUERY);
export const superPkgSchemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, SND_SUPER_PKG_QUERY);
export const topLevelSuperPkgSchemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, 'TopLevelSuperPkgs');
export const catSchemaQuery = SchemaQuery.create(SND_PKG_SCHEMA, SND_CATEGORY_QUERY);

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

export const defaultPackagesModel: PackagesModelProps = {
    active: [],
    data: {},
    dataIds: [],
    drafts: [],
    filteredActive: [],
    filteredDrafts: [],
    isError: false,
    isInit: false,
    isWarning: false,
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
    isWarning?: boolean;
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

export const defaultQuerySuperPackageModel: QuerySuperPackageModelProps = {
    Container: undefined,
    Created: undefined,
    CreatedBy: undefined,
    Modified: undefined,
    ModifiedBy: undefined,
    ParentSuperPkgId: undefined,
    SuperPkgPath: undefined,
    SuperPkgId: undefined,
    PkgId: undefined,
    IsPrimitive: undefined,
    Narrative: undefined,
    links: undefined
};

export class QuerySuperPackageModel implements QuerySuperPackageModelProps {
    Container: LabKeyQueryRowPropertyProps;
    Created: LabKeyQueryRowPropertyProps;
    CreatedBy: LabKeyQueryRowPropertyProps;
    Modified: LabKeyQueryRowPropertyProps;
    ModifiedBy: LabKeyQueryRowPropertyProps;
    ParentSuperPkgId: LabKeyQueryRowPropertyProps;
    SuperPkgPath: LabKeyQueryRowPropertyProps;
    SuperPkgId: LabKeyQueryRowPropertyProps;
    PkgId: LabKeyQueryRowPropertyProps;
    IsPrimitive: LabKeyQueryRowPropertyProps;
    Narrative: LabKeyQueryRowPropertyProps;
    links: any;

    constructor(values: QuerySuperPackageModelProps = defaultQuerySuperPackageModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
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