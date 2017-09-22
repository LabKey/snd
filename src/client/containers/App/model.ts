interface AppModelProps {
    isError?: boolean
    isWarning?: boolean
    message?: string
}

export class AppModel implements AppModelProps {

    isError?: boolean = false;
    isWarning?: boolean = false;
    message?: string = undefined;

    constructor(props?: Partial<AppModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}