import * as React from 'react';
import { Button, Modal } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux';

import { History } from 'history'

import { PackageSearchInput } from '../../../components/Packages/PackageSearchInput'
import { PackageSearchResults } from '../../../components/Packages/PackageSearchResults'
import * as actions from '../actions'
import { PackagesModel } from '../model'

import { QueryModel } from '../../../query/model'


interface PackageViewerOwnProps {
    history?: History
    model?: QueryModel
}

interface PackageViewerState {
    dispatch?: Dispatch<any>

    packagesModel?: PackagesModel
}

interface PackageViewerStateProps {
    toRemove?: number
}

type PackageViewerProps = PackageViewerOwnProps & PackageViewerState;

function mapStateToProps(state: APP_STATE_PROPS) {

    return {
        packagesModel: state.packages
    };
}

export class PackageViewerImpl extends React.Component<PackageViewerProps, PackageViewerStateProps> {

    private inputRef: HTMLInputElement;

    constructor(props: PackageViewerProps) {
        super(props);

        this.state = {
            toRemove: undefined
        };

        this.changeLocation = this.changeLocation.bind(this);
        this.deletePackage = this.deletePackage.bind(this);
        this.handleClear = this.handleClear.bind(this);
        this.handleDeleteRequest = this.handleDeleteRequest.bind(this);
        this.handleInputChange = this.handleInputChange.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.toggleDrafts = this.toggleDrafts.bind(this);
    }

    componentDidMount() {
        const { dispatch, model, packagesModel } = this.props;
        // make a should load/init function
        if (model && model.isLoaded && !packagesModel || packagesModel && !packagesModel.isInit) {
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

    componentWillUnmount() {
        const { dispatch } = this.props;
        dispatch(actions.resetPackageFilter());
    }

    changeLocation(loc: string) {
        const { history } = this.props;
        history.push(loc);
    }

    deletePackage() {
        const { dispatch } = this.props;
        const { toRemove } = this.state;
        dispatch(actions.deletePackage(toRemove));
    }

    handleClear() {
        const { dispatch } = this.props;

        this.inputRef.focus();
        dispatch(actions.filterPackages(''));
    }

    handleDeleteRequest(rowId) {
        const { dispatch } = this.props;

        this.setState({
            toRemove: rowId
        });

        dispatch(actions.packagesWarning())
    }

    hideModal() {
        const { dispatch } = this.props;

        dispatch(actions.packagesResetWarning())
    }

    handleInputChange(evt: React.ChangeEvent<HTMLInputElement>) {
        const { dispatch } = this.props;
        const input = evt.currentTarget.value;
        dispatch(actions.filterPackages(input));
    }

    toggleDrafts() {
        const { dispatch, packagesModel } = this.props;
        dispatch(packagesModel.toggleDrafts())
    }

    renderWarning() {
        const { isWarning } = this.props.packagesModel;
        const { toRemove } = this.state;

        if (isWarning) {
            return (
                <div className="static-modal">
                    <Modal onHide={this.hideModal} show={isWarning}>
                        <Modal.Body>
                            Are you sure you want to remove package {toRemove}?
                        </Modal.Body>
                        <Modal.Footer>
                            <Button onClick={this.hideModal}>Cancel</Button>
                            <Button bsStyle='primary' onClick={this.deletePackage}>Delete Package</Button>
                        </Modal.Footer>
                    </Modal>
                </div>
            )
        }
    }

    render() {

        if (this.props.packagesModel) {
            const { data, filteredActive, filteredDrafts, input, isInit, showDrafts } = this.props.packagesModel;

            return (
                <div className="row" style={{padding: '20px 0'}}>
                    {this.renderWarning()}
                    <PackageSearchInput
                        changeLocation={this.changeLocation}
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
                                        isLoaded={isInit}
                                        handleDelete={this.handleDeleteRequest}/>
                                </div>
                            </div>
                            : null}

                        <div className="package_viewer__results--active clearfix">
                            <h4>Active</h4>
                            <div className="package_viewer__results-container">
                                <PackageSearchResults
                                    data={data}
                                    dataIds={filteredActive}
                                    isLoaded={isInit}
                                    handleDelete={this.handleDeleteRequest}/>
                            </div>
                        </div>
                    </div>
                </div>
            )
        }

        return <div><i className="fa fa-spinner fa-spin fa-fw"/> Loading...</div>;
    }
}

export const PackageViewer = connect<any, any, PackageViewerProps>(mapStateToProps)(PackageViewerImpl);