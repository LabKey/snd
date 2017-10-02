import * as actions from './actions'
import { PACKAGE_VIEW } from '../../Packages/Forms/PackageFormContainer'
import { AssignedPackageModel } from '../../Packages/model'
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

interface PackageModelAttributeLookupProps {
    value: string
    label: string
}

interface PackageModelAttributeProps {
    defaultValue?: string
    format?: string
    label?: string
    lookupKey?: string
    lookupQuery?: string
    lookupSchema?: string
    lookupValues?: Array<string>
    max?: number
    min?: number
    name?: string
    rangeURI?: string
    redactedText?: string
    required?: boolean | 'on'
    scale?: number
    sortOrder?: number
    validators?: Array<PackageModelValidatorProps>
    [key: string]: any
}

export class PackageModelAttribute implements PackageModelAttributeProps {
    defaultValue: string = undefined;
    format: string = undefined;
    label: string = undefined;
    lookupKey: string = undefined;
    lookupQuery: string = undefined;
    lookupSchema: string = undefined;
    lookupValues: Array<string> = undefined;
    max: number = 0;
    min: number = 0;
    name: string = undefined;
    rangeURI: string = 'string';
    redactedText: string = undefined;
    required: boolean | 'on' = false;
    scale: number = 0;
    sortOrder: number = 0;
    validators: Array<PackageModelValidatorProps>;
    [key: string]: any;

    constructor(props?: Partial<PackageModelAttribute>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface PackageModelProps {
    active?: boolean
    attributes?: Array<PackageModelAttribute>
    attributeLookups?: Array<PackageModelAttributeLookupProps>
    categories?: Array<number>
    container?: string
    description?: string
    extraFields?: Array<PackageModelAttributeProps>
    hasEvent?: boolean;
    hasProject?: boolean;
    narrative?: string
    narrativeKeywords?: Array<string>
    pkgId?: number
    qcState?: any
    repeatable?: boolean
    subPackages?: Array<AssignedPackageModel>
}

export class PackageModel implements PackageModelProps {
    active: boolean = false;
    attributes: Array<PackageModelAttribute> = [];
    attributeLookups: Array<PackageModelAttributeLookupProps> = [];
    categories: Array<number> = [];
    container: string = undefined;
    description: string = undefined;
    extraFields: Array<PackageModelAttributeProps> = [];
    hasEvent: boolean = false;
    hasProject: boolean = false;
    narrative: string = undefined;
    narrativeKeywords: Array<string> = [];
    pkgId: number = undefined;
    qcState: any = null; // todo: find out qcState type
    repeatable: boolean = false;
    subPackages: Array<AssignedPackageModel> = [];

    constructor(props?: Partial<PackageModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface PackageSubmissionModelProps {
    active?: boolean;
    attributes?: Array<PackageModelAttribute>;
    categories?: Array<number>;
    clone?: boolean;
    container?: string;
    description?: string;
    extraFields?: Array<PackageModelAttributeProps>;
    id?: number;
    narrative?: string;
    pkgId?: number;
    qcState?: any;
    repeatable?: boolean;
    subPackages?: Array<{sortOrder: number, superPkgId: number}>;
}

export class PackageSubmissionModel implements PackageSubmissionModelProps {
    active: boolean = false;
    attributes: Array<PackageModelAttribute> = [];
    categories: Array<number> = [];
    clone: boolean = false;
    container: string = undefined;
    description: string = undefined;
    extraFields: Array<PackageModelAttributeProps> = [];
    id: number = undefined;
    narrative: string = undefined;
    pkgId: number = undefined;
    qcState: any = null;
    repeatable: boolean = false;
    subPackages: Array<{sortOrder: number, superPkgId: number}> = [];

    constructor(props: Partial<PackageSubmissionModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface PackageWizardModelProps {
    data?: PackageModel;
    formView?: PACKAGE_VIEW;
    initialData?: PackageModel;
    isActive?: boolean;
    isError?: boolean;
    isSubmitted?: boolean;
    isSubmitting?: boolean;
    isValid?: boolean;
    isWarning?: boolean;
    message?: string;
    packageCount?: number;
    packageId?: number
    packageLoaded?: boolean;
    packageLoading?: boolean;
}

export class PackageWizardModel implements PackageWizardModelProps {
    data: PackageModel = new PackageModel();
    formView: PACKAGE_VIEW = undefined;
    initialData: PackageModel = new PackageModel();
    isActive: boolean = false;
    isError: boolean = false;
    isSubmitted: boolean = false;
    isSubmitting: boolean = false;
    isValid: boolean = false;
    isWarning: boolean = false;
    message: string = undefined;
    packageCount: number = 0;
    packageId: number = undefined;
    packageLoaded: boolean = false;
    packageLoading: boolean = false;

    constructor(props: Partial<PackageWizardModelProps>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }

    checkValid() {
        return actions.packageCheckValid(this);
    }

    formatPackageValues(active: boolean): PackageSubmissionModel {
        return actions.formatPackageValues(this, active);
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

    setWarning(warning?: string) {
        return actions.packageWarning(this, warning);
    }

    submitForm(active: boolean, onSuccess?: any) {
        return actions.save(this, this.formatPackageValues(active), onSuccess);
    }

    success(response: PackageQueryResponse, view: PACKAGE_VIEW) {
        return actions.packageSuccess(this, response, view);
    }
}

interface PackageWizardContainerProps {
    packageData: {[key: string]: PackageWizardModel}
}

export class PackageWizardContainer implements PackageWizardContainerProps {
    packageData: {[key: string]: PackageWizardModel} = {};

    constructor(props?: Partial<PackageWizardContainer>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}