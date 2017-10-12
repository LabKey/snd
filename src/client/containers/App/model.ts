export interface AppMessage {
    id: any
    message: string
    role: 'success' | 'error' | 'warning'
}

interface AppModelProps {
    isError?: boolean
    isWarning?: boolean
    message?: string
    messages?: Array<AppMessage>
}

export class AppModel implements AppModelProps {

    isError?: boolean = false;
    isWarning?: boolean = false;
    message?: string = undefined;
    messages?: Array<AppMessage> = [];

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