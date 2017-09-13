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
    handleSelect?: any
    selected?: Array<number>
}

export class QuerySearchResults extends React.Component<QuerySearchResultsProps, {}> {

    render() {
        const { columns, data, dataIds, selected } = this.props;

        // todo: add onHover to change check to x for selected elements
        if (data && dataIds.length) {
            return (
                <div className='data-search__container' style={{maxHeight: '175px', overflow: 'scroll'}}>
                    {selected.map((id, i) => {
                        return (
                            <ListGroupItem
                                className={"data-search__row_" + id + i}
                                key={['selected_data', id, i].join('_')}
                                onClick={() => this.props.handleSelect(id)}>
                                {columns.map((column, j) => {
                                    const display = data[id][column.fieldKey.name].displayValue ?
                                        data[id][column.fieldKey.name].displayValue :
                                        data[id][column.fieldKey.name].value;

                                    return (
                                        <span key={j}>{display} </span>
                                    )
                                })}
                                <span>
                                    <i className="fa fa-check"/>
                                </span>
                            </ListGroupItem>
                        )
                    })}
                    {dataIds.map((id, i) => {
                        return (
                            <ListGroupItem
                                className={"data-search__row_" + id}
                                key={['data_option', id, i].join('_')}
                                onClick={() => this.props.handleSelect(id)}>
                                {columns.map((column, j) => {
                                    const display = data[id][column.fieldKey.name].displayValue ?
                                        data[id][column.fieldKey.name].displayValue :
                                        data[id][column.fieldKey.name].value;

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

    name?: string
    schemaQuery: SchemaQuery
    value?: Array<any>
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
    data?: Array<any>
    input?: string
    selected?: Array<number>
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
            data: [],
            input: '',
            selected: props.value
        };

        this.handleClear = this.handleClear.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
    }

    componentDidMount() {
        const { model, schemaQuery } = this.props;
        if (!model) {
            this.props.initModel(schemaQuery);
        }
    }

    componentWillReceiveProps(nextProps?: QuerySearchWrapperProps) {
        const { model, value } = nextProps;
        if (model && model.dataIds) {
            this.setState({
                data: model.dataIds,
                selected: value
            });
        }
    }

    filterResults(input, data, dataIds) {
        const { selected } = this.state;
        const columns = this.getColumns().map((col) => col.fieldKey.name);

        return dataIds.filter((id) => {
            if (selected.indexOf(id) === -1) {
                if (input) {
                    return columns.some((col) => {
                        const value = data[id][col]['value'];
                        return value.toString().toLowerCase().indexOf(input) !== -1;
                    });
                }
                return true;
            }
            return false;
        });
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

    handleSelect(id: number) {
        const { handleChange, name, model } = this.props;

        let selected: Array<number>;

        if (this.state.selected.indexOf(id) > -1) {
            let selectedState = [].concat(this.state.selected);
            selectedState.splice(selectedState.indexOf(id), 1);
            selected = selectedState;
        }
        else {
            selected = this.state.selected.concat(id);
        }

        this.setState({
            input: '',
            selected
        }, () => {
            this.setState({
                data: this.filterResults('', model.data, model.dataIds)
            });
        });

        if (handleChange && typeof handleChange === 'function') {
            handleChange(name, selected);
        }
    }

    setInput(input: string) {
        const { model } = this.props;

        this.setState({
            data: this.filterResults(input, model.data, model.dataIds),
            input
        });
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
        const { data, selected } = this.state;
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
                        columns={this.getColumns()}
                        data={model.data}
                        dataIds={data}
                        handleSelect={this.handleSelect}
                        selected={selected}/>
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