

import {fetchPackage} from "../Packages/actions";
import {PackageModel, PackageQueryResponse, PackageWizardModel} from "../Packages/model";
import {AssignedPackageModel} from "../../SuperPackages/model";
import {PKG_WIZARD_TYPES} from "../Packages/constants";



function getPackageModelFromResponse(response: PackageQueryResponse): PackageModel {
    // the response should have exactly one row
    return Array.isArray(response.json) && response.json.length == 1 ?
        response.json[0] :
        new PackageModel();
}

export function queryPackageFullNarrative(id: number, model: PackageWizardModel) {
    return (dispatch, getState) => {
        return fetchPackage(id, false, false).then((response: PackageQueryResponse) => {
            const packageModel = getPackageModelFromResponse(response);
            const narrativePkg = new AssignedPackageModel(
                packageModel.pkgId,
                packageModel.description,
                packageModel.narrative,
                packageModel.repeatable,
                undefined,
                undefined,
                packageModel.subPackages
            );

            dispatch({
                type: PKG_WIZARD_TYPES.PACKAGE_FULL_NARRATIVE,
                model,
                narrativePkg
            });
        }).catch((error) => {
            // set error
            console.log('error', error)
        });
    }
}

export function formatSubPackages(subPackages: Array<AssignedPackageModel>): Array<{sortOrder: number, superPkgId: number}> {

    if (subPackages.length) {
        return subPackages.map((s: AssignedPackageModel, i: number) => {
            return {
                sortOrder: i,
                superPkgId: s.SuperPkgId
            }
        });
    }

    return [];
}