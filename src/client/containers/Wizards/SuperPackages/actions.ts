

import {fetchPackage} from "../Packages/actions";
import {PackageModel, PackageQueryResponse, PackageWizardModel} from "../Packages/model";
import {AssignedPackageModel} from "../../SuperPackages/model";
import {ProjectWizardModel} from "../Projects/model";
import {SubPackageSubmissionModel} from "./model";



function getPackageModelFromResponse(response: PackageQueryResponse): PackageModel {
    // the response should have exactly one row
    return Array.isArray(response.json) && response.json.length == 1 ?
        response.json[0] :
        new PackageModel();
}

export function queryPackageFullNarrative(id: number, model: PackageWizardModel | ProjectWizardModel, dispatchType: string ) {
    return (dispatch) => {
        return fetchPackage(id, false, false).then((response: PackageQueryResponse) => {
            const packageModel = getPackageModelFromResponse(response);
            const narrativePkg = new AssignedPackageModel(
                packageModel.pkgId,
                packageModel.description,
                packageModel.narrative,
                packageModel.repeatable,
                undefined,
                true,
                true,
                undefined,
                packageModel.subPackages
            );

            dispatch({
                type: dispatchType,
                model,
                narrativePkg
            });
        }).catch((error) => {
            // set error
            console.log('error', error)
        });
    }
}

export function formatSubPackages(subPackages: Array<AssignedPackageModel>): Array<SubPackageSubmissionModel> {

    if (subPackages.length) {
        return subPackages.map((s: AssignedPackageModel, i: number) => {
            return new SubPackageSubmissionModel({
                sortOrder: i,
                active: s.active,
                superPkgId: s.superPkgId
            });
        });
    }

    return [];
}