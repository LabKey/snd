import * as actions from './actions'
import { PACKAGE_VIEW } from '../../Packages/Forms/PackageFormContainer'
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
    lookupValues?: Array<string>
    name?: string
    rangeURI?: string
    required?: boolean | 'on'
    scale?: number
    validators?: Array<PackageModelValidatorProps>
    [key: string]: any
}

export const defaultPackageModelAttribute: PackageModelAttributeProps = {
    format: undefined,
    label: undefined,
    lookupQuery: undefined,
    lookupSchema: undefined,
    lookupValues: undefined,
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
    lookupValues: Array<string>;
    name: string;
    rangeURI: string;
    required: boolean | 'on';
    scale: number;
    validators: Array<PackageModelValidatorProps>;
    [key: string]: any;

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
    extraFields?: Array<PackageModelAttributeProps>
    narrative?: string
    pkgId?: number
    qcState?: any
    repeatable?: boolean
    subPackages?: Array<number>
}

export const defaultPackageModel = {
    active: false,
    attributes: [],
    categories: [],
    container: undefined,
    description: undefined,
    extraFields: [],
    narrative: undefined,
    pkgId: undefined,
    qcState: null,
    repeatable: false,
    subPackages: []
};

export class PackageModel implements PackageModelProps {
    active?: boolean;
    attributes?: Array<PackageModelAttributeProps>;
    categories?: Array<number>;
    container?: string;
    description?: string;
    extraFields?: Array<PackageModelAttributeProps>;
    narrative?: string;
    pkgId?: number;
    qcState?: any;
    repeatable?: boolean;
    subPackages?: Array<number>;

    constructor(values: PackageModelProps = defaultPackageModel) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}

interface PackageSubmissionModelProps extends PackageModel {
    active?: boolean;
    attributes?: Array<PackageModelAttributeProps>;
    categories?: Array<number>;
    container?: string;
    description?: string;
    extraFields?: Array<PackageModelAttributeProps>;
    id?: number;
    narrative?: string;
    pkgId?: number;
    qcState?: any;
    repeatable?: boolean;
    subPackages?: Array<number>;
}

export class PackageSubmissionModel implements PackageSubmissionModelProps {
    active?: boolean;
    attributes?: Array<PackageModelAttributeProps>;
    categories: Array<number>;
    container?: string;
    description?: string;
    extraFields?: Array<PackageModelAttributeProps>;
    id?: number;
    narrative?: string;
    pkgId?: number;
    qcState?: any;
    repeatable?: boolean;
    subPackages?: Array<number>;

    constructor(values: PackageSubmissionModelProps) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}

interface PackageWizardModelProps {
    data?: PackageModel;
    formView?: PACKAGE_VIEW;
    initialData?: PackageModel;
    isActive?: boolean;
    isError?: boolean;
    isValid?: boolean;
    message?: string;
    packageCount?: number;
    packageId?: number
    packageLoaded?: boolean;
    packageLoading?: boolean;
}

export const defaultPackageWizardModel: PackageWizardModelProps = {
    data: new PackageModel(),
    formView: undefined,
    initialData: new PackageModel(),
    isActive: false,
    isError: false,
    isValid: false,
    message: undefined,
    packageCount: 0,
    packageId: undefined,
    packageLoaded: false,
    packageLoading: false,
};

export class PackageWizardModel implements PackageWizardModelProps {
    data?: PackageModel;
    formView?: PACKAGE_VIEW;
    initialData?: PackageModel;
    isActive?: boolean;
    isError?: boolean;
    isValid?: boolean;
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

    formatPackageValues(active: boolean, view?: PACKAGE_VIEW): PackageSubmissionModel {
        return actions.formatPackageValues(this, active, view);
    }

    loaded() {
        return actions.packageLoaded(this);
    }

    loading() {
        return actions.packageLoading(this);
    }

    saveField(name, value) {
        return actions.saveField(this, name, value);
    }

    saveNarrative(narrative: string) {
        return actions.saveNarrative(this, narrative);
    }

    setError(error) {
        return actions.packageError(this, error);
    }

    submitForm(active: boolean, onSuccess?: any) {
        return actions.save(this.formatPackageValues(active, this.formView), onSuccess);
    }

    success(response: PackageQueryResponse, view: PACKAGE_VIEW) {
        return actions.packageSuccess(this, response, view);
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