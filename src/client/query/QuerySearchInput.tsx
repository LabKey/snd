import * as React from 'react';
import { ListGroup, ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux'
import { Dispatch } from 'redux'

import * as actions from './actions'


import { QueryModel, SchemaQuery } from './model'


// Input
interface QuerySearchInputProps {
    handleClear: () => any
    handleInputChange: (event) => any
    inputValue?: string
    input?: any
}

export class QuerySearchInput extends React.Component<QuerySearchInputProps, {}> {

    constructor(props?: QuerySearchInputProps) {
        super(props);
    }

    render() {
        const { input, inputValue } = this.props;

        return (
            <ListGroupItem>
                <input
                    name="packageSearch"
                    onChange={(evt) => this.props.handleInputChange(evt)}
                    ref={input}
                    style={{width: '100%'}}
                    type="text"
                    value={inputValue}/>
            </ListGroupItem>
        )
    }
}

// Results
interface QuerySearchResultsProps {
    columns?: any
    data?: any
    dataIds?: Array<any>
}

export class QuerySearchResults extends React.Component<QuerySearchResultsProps, {}> {

    constructor(props?: QuerySearchResultsProps) {
        super(props);

    }

    render() {
        const { columns, data, dataIds } = this.props;

        if (data && dataIds.length) {
            return (
                <div className='data-search__container'>
                    {dataIds.map((id, i) => {
                        return (
                            <ListGroupItem className={"data-search__row_" + id} key={id}>
                                {columns.map((column, j) => {
                                    const display = data[id][column.fieldKey.name].displayValue ?
                                        data[id][column.fieldKey.name].displayValue :
                                        data[id][column.fieldKey.name].value;
                                    console.log(display)
                                    return (
                                        <span key={j}>{display} </span>
                                    )
                                })}
                            </ListGroupItem>
                        )
                    })}
                </div>

            )
        }

        return (
            <ListGroupItem className="data-search__container">
                <div className="data-search__row">
                    No results found
                </div>
            </ListGroupItem>
        )
    }
}



interface QuerySearchWrapperOwnProps {
    handleChange?: any

    schemaQuery: SchemaQuery
    // todo: support filters, etc

}

interface QuerySearchWrapperState {
    model?: QueryModel
}

interface QuerySearchWrapperDispatch {
    dispatch?: Dispatch<any>
    initModel: (schemaQuery: SchemaQuery) => any
}

interface QuerySearchWrapperStateProps {
    input?: string
}

type QuerySearchWrapperProps = QuerySearchWrapperOwnProps & QuerySearchWrapperState & QuerySearchWrapperDispatch;


function mapStateToProps(state: APP_STATE_PROPS, ownProps: QuerySearchWrapperOwnProps): QuerySearchWrapperState {
    const { schemaQuery } = ownProps;
    return {
        model: state.queries.models[schemaQuery.resolveKey()]
    }
}

function mapDispatchToProps(dispatch: Dispatch<any>): QuerySearchWrapperDispatch {
    return {
        initModel: (schemaQuery: SchemaQuery) => dispatch(actions.queryInitialize(schemaQuery)),
    }

}
// Wrapper
export class QuerySearchWrapperImpl extends React.Component<QuerySearchWrapperProps, QuerySearchWrapperStateProps> {

    private input: HTMLInputElement;
    private timer: number;

    constructor(props?: QuerySearchWrapperProps) {
        super(props);

        this.state = {
            input: ''
        };

        this.handleClear = this.handleClear.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    componentDidMount() {
        const { model, schemaQuery } = this.props;
        if (!model) {
            this.props.initModel(schemaQuery);
        }
    }

    getColumns() {
        const { model } = this.props;
        if (model.getColumn('Name').length) {
            return model.getColumn('Name');
        }

        return model.getKeyColumn().concat(model.getColumn('Description'));
    }

    handleClear() {
        this.setInput('');
        this.input.focus();
    }


    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const input = evt.currentTarget.value;
        this.setInput(input);
    }

    setInput(input: string) {
        this.setState({input});
    }

    render() {
        if (React.Children.count(this.props.children)) {
            return (
                <div>
                    {React.Children.map(this.props.children, (child: React.ReactElement<any>, index) => {
                        return React.cloneElement(child, {...this.props})
                    })}
                </div>
            )
        }

        // Needs work for styling, onClick handler, data filter, etc
        const { model } = this.props;
        if (model && model.isLoaded) {
            return (
                <ListGroup>
                    <QuerySearchInput
                        handleClear={this.handleClear}
                        handleInputChange={this.handleInputChange}
                        input={(el) => this.input = el}
                        inputValue={this.state.input}/>
                    <QuerySearchResults
                        data={model.data}
                        dataIds={model.dataIds}
                        columns={this.getColumns()}/>
                </ListGroup>
            );
        }

        return <div>Loading...</div>;
    }
}


export const QuerySearch = connect<QuerySearchWrapperState, QuerySearchWrapperDispatch, QuerySearchWrapperOwnProps>(
    mapStateToProps,
    mapDispatchToProps
)(QuerySearchWrapperImpl);