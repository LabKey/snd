import * as React from 'react';
import { Checkbox, ListGroupItem } from 'react-bootstrap'
import { connect } from 'react-redux';

import { QueryModel, SchemaQuery } from '../../../query/model'
import { PACKAGE_VIEW } from './PackageFormContainer'
import {SuperPackageSearchResults} from "../../../components/SuperPackages/SuperPackageSearchResults";
import {SearchInput} from "../../../components/Search/SearchInput";
import { QuerySuperPackageModel } from "../model";

interface SuperPackageViewerOwnProps {
    schemaQuery: SchemaQuery
    view?: PACKAGE_VIEW
}

interface SuperPackageViewerState {
    model?: QueryModel
}

interface SuperPackageViewerStateProps {
    input?: string
    filteredIds?: Array<number>
}

type SuperPackageViewerProps = SuperPackageViewerOwnProps & SuperPackageViewerState;

function mapStateToProps(state: APP_STATE_PROPS, ownProps: SuperPackageViewerOwnProps): SuperPackageViewerState {
    const { schemaQuery } = ownProps;

    return {
        model: state.queries.models[schemaQuery.resolveKey()]
    }
}

export class SuperPackageViewerImpl extends React.Component<SuperPackageViewerProps, SuperPackageViewerStateProps> {

    private timer: number = 0;
    private inputRef: HTMLInputElement;

    constructor(props: SuperPackageViewerProps) {
        super(props);

        this.state = {
            input: '',
            filteredIds: undefined
        };

        this.handleClear = this.handleClear.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
    }

    handleClear() {
        this.setInput('');
        this.setFilteredIds(undefined);
        this.inputRef.focus();
    }

    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const input = evt.currentTarget.value;

        this.setInput(input);

        clearTimeout(this.timer);
        this.timer = setTimeout(() => {
            this.timer = null;
            this.updateFilterIds();
        }, 50);
    }

    updateFilterIds() {
        const { model } = this.props;
        const { input } = this.state;
        let filterIds: Array<number>;

        if (model != undefined && Array.isArray(model.dataIds) && model.data) {
            filterIds =  model.dataIds.filter((id: number) => {
                const superPkg: any = model.data[id]; // TODO why doesn't QuerySuperPackageModel work instead of any?

                if (superPkg) {
                    return (
                            superPkg.PkgId &&
                            superPkg.PkgId.displayValue.toLowerCase().indexOf(input.toLowerCase()) !== -1
                        ) || (
                            superPkg.SuperPkgId &&
                            superPkg.SuperPkgId.value.toString().indexOf(input) !== -1
                        )
                }
           });
        }

        this.setFilteredIds(filterIds);
    }

    setInput(input: string) {
        this.setState({input});
    }

    setFilteredIds(filteredIds: Array<number>) {
        this.setState({filteredIds});
    }

    render() {
        const { model } = this.props;
        const { input, filteredIds } = this.state;

        if (model != undefined && model.data != undefined) {
            return (
                <div>
                    <ListGroupItem className="col-sm-12" style={{height: '55px'}}>
                        <SearchInput className="col-sm-8"
                            inputRef={(el) => this.inputRef = el}
                            input={input}
                            handleClear={this.handleClear}
                            handleInputChange={this.handleInputChange}
                        />
                        <Checkbox disabled className="col-sm-4">
                            Primatives only
                        </Checkbox>
                    </ListGroupItem>
                    <SuperPackageSearchResults
                        data={model.data}
                        dataIds={Array.isArray(filteredIds) ? filteredIds : model.dataIds}
                        isLoaded={model.isLoaded}/>
                </div>
            )
        }

        return (
            <ListGroupItem className="data-search__container" style={{height: '250px'}}>
                <div className="data-search__row">
                    Loading...
                </div>
            </ListGroupItem>
        )
    }
}

export const SuperPackageViewer = connect<any, any, SuperPackageViewerProps>(mapStateToProps)(SuperPackageViewerImpl);