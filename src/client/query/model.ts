

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

interface LabKeyQueryFieldProps {
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
    schema?: string
    query?: string
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

const defaultQueryModel: QueryModelProps = {
    schema: undefined,
    query: undefined,
    view: undefined,

    data: {},
    dataCount: 0,
    dataIds: [],
    filters: [], // define filter type
    isError: false,
    isLoaded: false,
    isLoading: false,
    message: undefined,
    metaData: {} as LabKeyQueryMetaDataProps
};

export class QueryModel implements QueryModelProps {
    schema?: string;
    query?: string;
    view?: string;

    data?: {[key: string]: LabKeyQueryRowPropertyProps};
    dataCount?: number;
    dataIds?: Array<number>;
    filters?: Array<any>; // define filter type
    isError?: boolean;
    isLoaded?: boolean;
    isLoading?: boolean;
    message?: string;
    metaData?: LabKeyQueryMetaDataProps;

    constructor(values: QueryModelProps = defaultQueryModel) {
        const data: QueryModelProps = Object.assign({}, defaultQueryModel, values);
        Object.keys(data).forEach(key => {
            this[key] = values[key];
        });
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

const defaultQueryModelsContainer: QueryModelsProps = {
    models: {},
    loadedQueries: [],
    loadingQueries: [],
};

export class QueryModelsContainer implements QueryModelsProps {
    models: {[key: string]: QueryModel};
    loadedQueries: Array<string>;
    loadingQueries: Array<string>;

    constructor(values: QueryModelsProps = defaultQueryModelsContainer) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}

interface SchemaQueryProps {
    schemaName: string
    queryName: string
    viewName?: string
}

export class SchemaQuery implements SchemaQueryProps {
    schemaName: string;
    queryName: string;
    viewName?: string;

    static create(schemaName: string, queryName: string, viewName?: string): SchemaQuery {
        return new SchemaQuery({schemaName, queryName, viewName});
    }

    constructor(values: SchemaQueryProps) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }

    resolveKey(): string {
        return resolveKey(this.schemaName, this.queryName, this.viewName);
    }
}

function resolveKey(schema: string, query: string, view?: string): string {
    let key = [schema, query].join('|');

    if (view != undefined) {
        key = [key, view].join('|');
    }

    return key.toLowerCase();
}