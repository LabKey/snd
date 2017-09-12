import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { PackageSearchInput } from '../../../components/Packages/PackageSearchInput'
import { PackageSearchResults } from '../../../components/Packages/PackageSearchResults'
import * as actions from '../actions'
import { PackagesModel } from '../model'

import { QueryModel } from '../../../query/model'


interface PackageViewerOwnProps {
    model?: QueryModel
}

interface PackageViewerState {
    dispatch?: Dispatch<any>

    packagesModel?: PackagesModel
}

interface PackageViewerStateProps {
    input?: string
}

type PackageViewerProps = PackageViewerOwnProps & PackageViewerState;

function mapStateToProps(state: APP_STATE_PROPS) {

    return {
        packagesModel: state.packages
    };
}

export class PackageViewerImpl extends React.Component<PackageViewerProps, PackageViewerStateProps> {

    private timer: number = 0;
    private inputRef: HTMLInputElement;

    constructor(props: PackageViewerProps) {
        super(props);

        this.state = {
            input: ''
        };

        this.handleClear = this.handleClear.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.toggleDrafts = this.toggleDrafts.bind(this);
    }

    componentDidMount() {
        const { dispatch, model, packagesModel } = this.props;
        if (model && model.isLoaded && !packagesModel) {
            dispatch(packagesModel.init(model));
        }
        // add mapDispatchToProps
    }

    componentWillReceiveProps(nextProps?: PackageViewerProps) {
        const { dispatch, model, packagesModel } = nextProps;
        const dataExists = (model && model.isLoaded);
        const modelExists = (packagesModel && packagesModel.isInit);

        if (dataExists && !modelExists) {
            dispatch(packagesModel.init(model));
        }
    }

    handleClear() {
        const { dispatch } = this.props;

        this.setInput('');

        this.inputRef.focus();
        dispatch(actions.filterPackages(''));
    }

    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const { dispatch } = this.props;
        const input = evt.currentTarget.value;

        this.setInput(input);

        clearTimeout(this.timer);
        this.timer = setTimeout(() => {
            this.timer = null;

            dispatch(actions.filterPackages(input))
        }, 50);
    }

    setInput(input: string) {
        this.setState({input});
    }

    toggleDrafts() {
        const { dispatch, packagesModel } = this.props;
        dispatch(packagesModel.toggleDrafts())
    }

    render() {

        if (this.props.packagesModel) {
            const { data, filteredActive, filteredDrafts, isInit, showDrafts } = this.props.packagesModel;
            const { input } = this.state;

            return (
                <div className="row" style={{padding: '20px 0'}}>
                    <PackageSearchInput
                        handleClear={this.handleClear}
                        handleInputChange={this.handleInputChange}
                        input={input}
                        inputRef={(el) => this.inputRef = el}
                        showDrafts={showDrafts}
                        toggleDrafts={this.toggleDrafts}/>

                    <div className="col-sm-12 package-viewer__results" style={{margin: '0 0 0 2%'}}>
                        {showDrafts ?
                            <div className="package_viewer__results--drafts">
                                <h4>Drafts</h4>
                                <div className="package_viewer__results-container">
                                    <PackageSearchResults
                                        data={data}
                                        dataIds={filteredDrafts}
                                        isLoaded={isInit}/>
                                </div>
                            </div>
                            : null}

                        <div className="package_viewer__results--active clearfix">
                            <h4>Active</h4>
                            <div className="package_viewer__results-container">
                                <PackageSearchResults
                                    data={data}
                                    dataIds={filteredActive}
                                    isLoaded={isInit}/>
                            </div>
                        </div>
                    </div>
                </div>
            )
        }

        return <div>Loading...</div>;
    }
}

export const PackageViewer = connect<any, any, PackageViewerProps>(mapStateToProps)(PackageViewerImpl);