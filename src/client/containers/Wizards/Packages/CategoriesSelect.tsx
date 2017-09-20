import * as React from 'react';
import { ControlLabel } from 'react-bootstrap'
import { Dispatch } from 'redux'

import { QueryModel } from '../../../query/model'

import { SearchInput } from '../../../components/Search/SearchInput'
import { CategoriesSelectResults } from '../../../components/Categories/CategoriesSelectResults'
import { arraysMatch } from '../../../utils/actions'

const styles = require<any>('../../Packages/Forms/PackageForm.css');

interface CategoriesSelectOwnProps {
    disabled?: boolean
    handleChange?: any // type this
    model?: QueryModel
    values?: any // type this
}

interface CategoriesSelectState {
    dispatch?: Dispatch<any>
}

interface CategoriesSelectStateProps {
    data?: Array<any>
    focused?: boolean
    input?: string
    selected?: Array<number>
}


type CategoriesSelectProps = CategoriesSelectOwnProps & CategoriesSelectState;

export class CategoriesSelect extends React.Component<CategoriesSelectProps, CategoriesSelectStateProps> {

    private inputRef: HTMLInputElement;
    private wrapper: HTMLDivElement;

    constructor(props?) {
        super(props);

        this.state = {
            data: props.model.dataIds,
            focused: false,
            input: '',
            selected: props.values
        };

        this.handleClickOutside = this.handleClickOutside.bind(this);
        this.handleClear = this.handleClear.bind(this);
        this.handleFocus = this.handleFocus.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
    }

    componentDidMount() {
        document.addEventListener('mousedown', this.handleClickOutside);
    }

    componentWillUnmount() {
        document.removeEventListener('mousedown', this.handleClickOutside);
    }

    componentWillReceiveProps(nextProps?: CategoriesSelectProps) {
        const { model, values } = nextProps;

        if (model && model.dataIds && !arraysMatch(model.dataIds, this.state.data)) {
            this.setState({
                data: model.dataIds,
                selected: values
            });
        }
    }

    filterResults(input, data, dataIds) {
        const { selected } = this.state;

        return dataIds.filter((id) => {
            if (selected.indexOf(id) === -1) {
                if (input) {
                    const inputVal = input.toLowerCase();
                    const catId = data[id]['CategoryId']['value'],
                        description = data[id]['Description']['value'];

                    return catId && catId.toString().indexOf(inputVal) !== -1 ||
                        description && description.toLowerCase().indexOf(inputVal) !== -1;

                }
                return true;
            }
            return false;
        });
    }

    handleClear() {
        this.setInput('');
        this.inputRef.focus();
    }

    handleClickOutside(evt: MouseEvent) {
        if (this.wrapper && !this.wrapper.contains(evt.target as any)) {
            this.setState({focused: false});
        }
    }

    handleFocus(el: React.FocusEvent<HTMLInputElement>) {
        const { focused } = this.state;
        if (!focused) {
            this.setState({focused: true})
        }
    }


    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const input = evt.currentTarget.value;
        this.setInput(input);
    }

    handleSelect(id: number) {
        const { handleChange, model } = this.props;

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
            const name = 'categories';
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
        const { disabled, model } = this.props;
        const { data, focused, input, selected } = this.state;
        return (
            <div className='row input-row' ref={(el) => this.wrapper = el}>
                <div className={"clearfix col-sm-12 " + styles['margin-top']}>
                    <ControlLabel>Categories</ControlLabel >
                </div>
                <SearchInput
                    disabled={disabled}
                    handleClear={this.handleClear}
                    handleFocus={this.handleFocus}
                    handleInputChange={this.handleInputChange}
                    input={input}
                    inputRef={(el) => this.inputRef = el}
                    name="query-search-input"
                    wrapperClassName="col-sm-12 clearfix"/>
                <CategoriesSelectResults
                    data={model.data}
                    dataIds={data}
                    disabled={disabled}
                    handleSelect={this.handleSelect}
                    focused={focused}
                    selected={selected}/>
            </div>
        )
    }
}