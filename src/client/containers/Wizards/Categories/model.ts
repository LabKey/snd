

interface CategoryWizardContainerProps {
    categoryData: {[key: string]: any}
}

export class CategoryWizardContainer implements CategoryWizardContainerProps {
    categoryData: {[key: string]: any} = {};

    constructor(props?: Partial<CategoryWizardContainer>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}
//
// interface EditableQueryModelProps extends QueryModelProps {}
//
// export class EditableQueryModel implements EditableQueryModelProps {
//     schemaQuery?: SchemaQuery = new SchemaQuery();
//     schema?: string = undefined;
//     query?: string = undefined;
//
//     data?: {[key: string]: LabKeyQueryRowPropertyProps} = {};
//     dataCount?: number = 0;
//     dataIds?: Array<number> = [];
//     filters?: Array<any> = []; // define filter type
//     isError?: boolean = false;
//     isLoaded?: boolean = false;
//     isLoading?: boolean = false;
//     message?: string = undefined;
//     metaData?: LabKeyQueryMetaDataProps = {} as LabKeyQueryMetaDataProps;
//
//     constructor(props?: Partial<EditableQueryModel>) {
//         if (props) {
//             for (let k in props) {
//                 if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
//                     this[k] = props[k];
//                 }
//             }
//         }
//     }
//
//     addRow() {
//         return actions.queryEditAddRow(this.schemaQuery);
//     }
//
//     removeRow(rowId: number) {
//         return actions.queryEditRemoveRow(this.schemaQuery, rowId);
//     }
// }
