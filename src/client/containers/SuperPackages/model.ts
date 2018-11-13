/*
 * Copyright (c) 2018 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


interface AssignedPackageModelProps {
    description: string
    narrative: string
    pkgId: number
    repeatable: boolean
    sortOrder: number
    required: boolean
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
    required: boolean;
    subPackages: Array<AssignedPackageModel> = [];
    superPkgId: number = undefined;
    active: boolean;
    showActive: boolean;

    // set the altId as a way to uniquely remove this assigned package or to handle assigned package click
    altId: number = LABKEY.Utils.id();

    // set to true to indicate that a package is in the process of loading the full hierarchy
    loadingSubpackages: boolean = undefined;

    constructor(pkgId: number, description: string, narrative: string, repeatable: boolean, superPkgId: number,
                active: boolean, showActive: boolean, required: boolean, sortOrder?: number, subPackages?: Array<AssignedPackageModel>)
    {
        this.pkgId = pkgId;
        this.description = description;
        this.narrative = narrative;
        this.repeatable = repeatable;
        this.sortOrder = sortOrder;
        this.superPkgId = superPkgId;
        this.active = active;
        this.showActive = showActive;
        this.required = required;
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