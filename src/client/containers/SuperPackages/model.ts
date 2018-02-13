

interface AssignedPackageModelProps {
    description: string
    narrative: string
    pkgId: number
    repeatable: boolean
    sortOrder: number
    subPackages: Array<AssignedPackageModel>
    superPkgId: number
    active: boolean
    showActive: boolean
    altId: number
    loadingSubpackages: boolean
}

export class AssignedPackageModel implements AssignedPackageModelProps {
    description: string = undefined;
    narrative: string = null;
    pkgId: number = undefined;
    repeatable: boolean = undefined;
    sortOrder: number = undefined;
    subPackages: Array<AssignedPackageModel> = [];
    superPkgId: number = undefined;
    active: boolean;
    showActive: boolean;

    // set the altId as a way to uniquely remove this assigned package or to handle assigned package click
    altId: number = LABKEY.Utils.id();

    // set to true to indicate that a package is in the process of loading the full hierarchy
    loadingSubpackages: boolean = undefined;

    constructor(pkgId: number, description: string, narrative: string, repeatable: boolean, superPkgId: number,
                active: boolean, showActive: boolean, sortOrder?: number, subPackages?: Array<AssignedPackageModel>)
    {
        this.pkgId = pkgId;
        this.description = description;
        this.narrative = narrative;
        this.repeatable = repeatable;
        this.sortOrder = sortOrder;
        this.superPkgId = superPkgId;
        this.active = active;
        this.showActive = showActive;
        if (Array.isArray(subPackages))
            this.subPackages = subPackages;
    }
}

interface AssignedSuperPackageModelProps {
    Active: boolean
    AssignedPackage: AssignedPackageModel
}

export class AssignedSuperPackageModel implements AssignedSuperPackageModelProps {
    Active: boolean = undefined;
    AssignedPackage: AssignedPackageModel = undefined;

    constructor(active: boolean, pkg: AssignedPackageModel)
    {
        this.Active = active;
        this.AssignedPackage = pkg;
    }
}