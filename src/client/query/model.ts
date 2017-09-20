import * as actions from './actions'
interface SchemaQueryParamsProps {
    editable?: boolean
}

interface SchemaQueryProps {
    params?: SchemaQueryParamsProps
    queryName?: string
    schemaName?: string
    viewName?: string
}

export class SchemaQuery implements SchemaQueryProps {
    params?: SchemaQueryParamsProps = {editable: false};
    queryName?: string = undefined;
    schemaName?: string = undefined;
    viewName?: string = undefined;

    static create(schemaName: string, queryName: string, viewName?: string, params?: SchemaQueryParamsProps): SchemaQuery {
        return new SchemaQuery({schemaName, queryName, viewName, params});
    }

    constructor(props?: Partial<SchemaQuery>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }

    resolveKey(): string {
        return [this.schemaName, this.queryName].join('|').toLowerCase();
    }
}

export interface LabKeyQueryRowPropertyProps {
    displayValue?: string
    url?: string
    value: any
}

interface LabKeyQueryColumnModelProps {
    align: string
    dataIndex: string
    editable: boolean
    header: string
    hidden: boolean
    required: boolean
    scale: number
    sortable: boolean
    width: number
}

export interface LabKeyQueryFieldProps {
    align: string
    autoIncrement: boolean
    calculated: boolean
    caption: string
    cols: number
    conceptURI: any
    defaultScale: string
    defaultValue: any
    dimension: boolean
    excludeFromShifting: boolean
    facetingBehaviorType: string
    fieldKey: {
        name: string
        parent: any
    }
    friendlyType: string
    hidden: boolean
    inputType: string
    jsonType: string
    keyField: boolean
    measure: boolean
    mvEnabled: boolean
    nullable: boolean
    phi: string
    protected: boolean
    readOnly: boolean
    recommendedVariable: boolean
    scale: number
    selectable: boolean
    shortCaption: string
    shownInDetailsView: boolean
    shownInInsertView: boolean
    shownInUpdateView: boolean
    sortable: boolean
    sqlType: string
    type: string
    userEditable: boolean
    versionField: boolean
    width: number
}

export interface LabKeyQueryMetaDataProps {
    description: string
    fields: Array<LabKeyQueryFieldProps>
    id: string
    importMessage: string
    importTemplates: Array<{label: string, url: string}>
    root: string
    title: string
    totalProperty: string
}

export interface LabKeyQueryResponse {
    columnModel?: Array<LabKeyQueryColumnModelProps>
    metaData?: LabKeyQueryMetaDataProps
    queryName: string
    rowCount: number
    rows: Array<{[key: string]: LabKeyQueryRowPropertyProps}>
    schemaKey: {name: string, parent: any}
    schemaName: Array<string> | string
}


export interface QueryModelProps {
    id: string
    query?: string
    requiredColumns?: Array<string>
    schemaQuery?: SchemaQuery
    schema?: string
    view?: string

    data?: {[key: string]: LabKeyQueryRowPropertyProps}
    dataCount?: number
    dataIds?: Array<number>
    filters?: Array<any> // define filter type
    isError?: boolean
    isLoaded?: boolean
    isLoading?: boolean
    message?: string
    metaData?: LabKeyQueryMetaDataProps
}

export class QueryModel implements QueryModelProps {
    id: string = undefined;
    query?: string = undefined;
    requiredColumns?: Array<string> = [];
    schemaQuery?: SchemaQuery = new SchemaQuery();
    schema?: string = undefined;
    view?: string = undefined;

    data?: {[key: string]: LabKeyQueryRowPropertyProps} = {};
    dataCount?: number = 0;
    dataIds?: Array<number> = [];
    filters?: Array<any> = []; // define filter type
    isError?: boolean = false;
    isLoaded?: boolean = false;
    isLoading?: boolean = false;
    message?: string = undefined;
    metaData?: LabKeyQueryMetaDataProps = {} as LabKeyQueryMetaDataProps;

    constructor(props?: Partial<QueryModel>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }

    init() {
        return actions.init(this);
    }

    load() {
        return actions.load(this);
    }

    getColumn(columnName: string) {
        if (this.metaData) {
            return this.metaData.fields.filter((field: LabKeyQueryFieldProps) => {
                if (field.fieldKey && field.fieldKey.name) {
                    return field.fieldKey.name === columnName;
                }
            });
        }

        return [];
    }

    getKeyColumn(): Array<LabKeyQueryFieldProps> {
        if (this.metaData) {
            return this.metaData.fields.filter((field: LabKeyQueryFieldProps) => {
                return field.keyField;
            });
        }

        return [];
    }

    getRequiredColumns(): string | Array<string> {
        return this.requiredColumns.length ? this.requiredColumns : '*';
    }

    // TODO: add a fetch for LABKEY.Query.getQueryDetails and populate a queryInfo field instead of metaData
    getVisibleColumns(): Array<LabKeyQueryFieldProps> {
        if (this.metaData) {
            return this.metaData.fields.filter((field: LabKeyQueryFieldProps) => {
                return field.shownInDetailsView;
            });
        }

        return [];
    }

    getVisibleColumnNames(): Array<string> {
        return this.getVisibleColumns().map(col => col.caption);
    }
}

export interface QueryModelsProps {
    models: {[key: string]: QueryModel}
    loadedQueries: Array<string>
    loadingQueries: Array<string>
}

export class QueryModelsContainer implements QueryModelsProps {
    models: {[key: string]: QueryModel} = {};
    loadedQueries: Array<string> = [];
    loadingQueries: Array<string> = [];

    constructor(props?: Partial<QueryModelsContainer>) {
        if (props) {
            for (let k in props) {
                if (this.hasOwnProperty(k) && props.hasOwnProperty(k)) {
                    this[k] = props[k];
                }
            }
        }
    }
}