

interface AssignedPackageModelProps {
    Description: string
    Narrative: string
    PkgId: number
    Repeatable: boolean
    SortOrder: number
    SubPackages: Array<AssignedPackageModel>
    SuperPkgId: number

    altId: number
    loadingSubpackages: boolean
}

export class AssignedPackageModel implements AssignedPackageModelProps {
    Description: string = undefined;
    Narrative: string = null;
    PkgId: number = undefined;
    Repeatable: boolean = undefined;
    SortOrder: number = undefined;
    SubPackages: Array<AssignedPackageModel> = [];
    SuperPkgId: number = undefined;

    // set the altId as a way to uniquely remove this assigned package or to handle assigned package click
    altId: number = LABKEY.Utils.id();

    // set to true to indicate that a package is in the process of loading the full hierarchy
    loadingSubpackages: boolean = undefined;

    constructor(pkgId: number, description: string, narrative: string, repeatable:
        boolean, superPkgId: number, sortOrder?: number, subPackages?: Array<AssignedPackageModel>)
    {
        this.PkgId = pkgId;
        this.Description = description;
        this.Narrative = narrative;
        this.Repeatable = repeatable;
        this.SortOrder = sortOrder;
        this.SuperPkgId = superPkgId;
        if (Array.isArray(subPackages))
            this.SubPackages = subPackages;
    }
}