

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

interface LabKeyQueryMetaDataProps {
    description: string
    fields: Array<any>
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
}

export interface QueryModelsProps {
    data: {[key: string]: QueryModel}
    loadedQueries: Array<string>
    loadingQueries: Array<string>
}

const defaultQueryModelsContainer: QueryModelsProps = {
    data: {},
    loadedQueries: [],
    loadingQueries: []
};

export class QueryModelsContainer implements QueryModelsProps {
    data: {[key: string]: QueryModel};
    loadedQueries: Array<string>;
    loadingQueries: Array<string>;

    constructor(values: QueryModelsProps = defaultQueryModelsContainer) {
        Object.keys(values).forEach(key => {
            this[key] = values[key];
        });
    }
}