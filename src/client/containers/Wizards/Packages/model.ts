import * as actions from './actions'

interface PackageQueryResponse {
    json: Array<PackageModel>
}

interface PackageModelValidatorProps {
    description: string
    errorMessage: string
    expression: string
    name: string
    type: string
}

interface PackageModelAttributeProps {
    format?: string
    label?: string
    lookupQuery?: string
    lookupSchema?: string
    name?: string
    rangeURI?: string
    required?: boolean
    scale?: number
    validators?: Array<PackageModelValidatorProps>
}

const defaultPackageModelAttribute: PackageModelAttributeProps = {
    format: undefined,
    label: undefined,
    lookupQuery: undefined,
    lookupSchema: undefined,
    name: undefined,
    rangeURI: undefined,
    required: false,
    scale: 0,
    validators: Array<PackageModelValidatorProps>(),
};
export class PackageModelAttribute implements PackageModelAttributeProps {

    format: string;
    label: string;
    lookupQuery: string;
    lookupSchema: string;
    name: string;
    rangeURI: string;
    required: boolean;
    scale: number;
    validators: Array<PackageModelValidatorProps>;

    constructor(values: PackageModelAttributeProps = defaultPackageModelAttribute) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}

interface PackageModelProps {
    active?: boolean
    attributes?: Array<PackageModelAttributeProps>
    categories?: Array<number>
    container?: string
    description?: string
    narrative?: string
    pkgId: number
    qcState?: any
    repeatable?: boolean
}

export const defaultPackageModel = {
    active: false,
    attributes: [],
    categories: [],
    container: undefined,
    description: undefined,
    narrative: undefined,
    pkgId: undefined,
    qcState: null,
    repeatable: false
};

export class PackageModel implements PackageModelProps {
    active: boolean;
    attributes: Array<PackageModelAttributeProps>;
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

interface PackageWizardModelProps {
    data?: PackageModel;
    isActive?: boolean;
    isError?: boolean;
    message?: string;
    packageCount?: number;
    packageId?: number
    packageLoaded?: boolean;
    packageLoading?: boolean;
}

export const defaultPackageWizardModel: PackageWizardModelProps = {
    data: new PackageModel(),
    isActive: false,
    isError: false,
    message: undefined,
    packageCount: 0,
    packageId: undefined,
    packageLoaded: false,
    packageLoading: false
};

export class PackageWizardModel implements PackageWizardModelProps {
    data?: PackageModel;
    isActive?: boolean;
    isError?: boolean;
    message?: string;
    packageCount?: number;
    packageId?: number;
    packageLoaded?: boolean;
    packageLoading?: boolean;

    constructor(values: PackageWizardModelProps = defaultPackageWizardModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }

    loaded() {
        return actions.packageLoaded(this);
    }

    loading() {
        return actions.packageLoading(this);
    }

    saveNarrative(narrative: string) {
        return actions.saveNarrative(this, narrative);
    }

    setError(error) {
        return actions.packageError(this, error);
    }

    success(response: PackageQueryResponse) {
        return actions.packageSuccess(this, response);
    }
}

interface PackageWizardContainerProps {
    packageData: {[key: string]: PackageWizardModel}
}

export const defaultPackageWizardContainer = {
    packageData: {}
};

export class PackageWizardContainer implements PackageWizardContainerProps {
    packageData: {[key: string]: PackageWizardModel};

    constructor(values: PackageWizardContainerProps = defaultPackageWizardContainer) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}