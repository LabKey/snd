
interface PropertyValidatorProps {
    description: string
    errorMessage: string
    expression: string
    name: string
    type: string
}

export class PropertyValidator implements PropertyValidatorProps {
    description: string = undefined;
    errorMessage: string = undefined;
    expression: string = undefined;
    name: string = undefined;
    type: string = undefined;

    constructor(props?: Partial<PropertyValidator>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}

interface PropertyDescriptorProps {
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
    required?: boolean
    scale?: number
    sortOrder?: number
    validators?: Array<PropertyValidatorProps>
    value?: any
    [key: string]: any
}

export class PropertyDescriptor implements PropertyDescriptorProps {
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
    required: boolean = false;
    scale: number = 0;
    sortOrder: number = 0;
    validators: Array<PropertyValidatorProps> = [];
    value: any = undefined;
    [key: string]: any;

    constructor(props?: Partial<PropertyDescriptor>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}