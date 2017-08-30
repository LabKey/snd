import { LabKeyQueryRowPropertyProps } from '../../query/model'


interface PackagesModelProps {
    active: Array<any>
    data: {[key: string]: any}
    dataIds: Array<any>
    drafts: Array<any>
    filteredActive?: Array<any>
    filteredDrafts?: Array<any>
    hasInput?: boolean

    isError: boolean
    isLoaded: boolean
    isLoading: boolean

    message: string
    packageCount: number
    packageData: {[key: string]: PackageModel}
    packageLoaded: boolean
    packageLoading: boolean
}

export const defaultPackagesModel: PackagesModelProps = {
    active: [],
    data: {},
    dataIds: [],
    drafts: [],
    filteredActive: [],
    filteredDrafts: [],
    hasInput: false,
    isError: false,
    isLoaded: false,
    isLoading: false,
    message: undefined,
    packageCount: 0,
    packageData: {},
    packageLoaded: false,
    packageLoading: false
};

export class PackagesModel implements PackagesModelProps {
    active: Array<any>;
    data: {[key: string]: any};
    dataIds: Array<any>;
    drafts: Array<any>;
    filteredActive?: Array<any>;
    filteredDrafts?: Array<any>;
    hasInput?: boolean;
    isError: boolean;
    isLoaded: boolean;
    isLoading: boolean;
    message: string;
    packageCount: number;
    packageData: {[key: string]: PackageModel};

    packageLoaded: boolean
    packageLoading: boolean

    constructor(values: PackagesModelProps = defaultPackagesModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
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

interface PackageModelValidatorProps {
    description: string
    errorMessage: string
    expression: string
    name: string
    type: string
}

interface PackageModelAttributeProps {
    format: string
    label: string
    lookupQuery: string
    lookupSchema: string
    name: string
    rangeURI: string
    required: boolean
    scale: number
    validators: Array<PackageModelValidatorProps>
}

interface PackageModelProps {
    active: boolean
    attributes: Array<PackageModelAttributeProps>
    categories: Array<number>
    container: string
    description: string
    narrative: string
    pkgId: number
    qcState: any
    repeatable: boolean
}

export const defaultPackageModel = {
    active: false,
    attributes: [],
    categories: [],
    container: undefined,
    description: undefined,
    narrative: undefined,
    pkgId: 0,
    qcState: null,
    repeatable: false
};

export class PackageModel implements PackageModelProps {

    active: boolean;
    attributes: Array<{
        format: string;
        label: string;
        lookupQuery: string;
        lookupSchema: string;
        name: string;
        rangeURI: string;
        required: boolean;
        scale: number;
        validators: Array<{
            description: string;
            errorMessage: string;
            expression: string;
            name: string;
            type: string;
        }>;
    }>;
    categories: Array<number>;
    container: string;
    description: string;
    narrative: string;
    pkgId: number;
    qcState: any;
    repeatable: boolean;

    constructor(values: PackageModelProps = defaultPackageModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}