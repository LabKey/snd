

interface SubPackageSubmissionModelProps {
    active: boolean;
    superPkgId: number;
    sortOrder: number;
    required: boolean;
}

export class SubPackageSubmissionModel implements SubPackageSubmissionModelProps {
    active: boolean = undefined;
    superPkgId: number = undefined;
    sortOrder: number = undefined;
    required: boolean = undefined;

    constructor(props: Partial<SubPackageSubmissionModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}