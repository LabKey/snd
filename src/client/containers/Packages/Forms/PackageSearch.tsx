import * as React from 'react';

import { CSSProperties } from 'react'
import { Button, DropdownButton, MenuItem } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { connect } from 'react-redux'
import { Dispatch } from 'redux'

import * as actions from '../actions'
import { PackagesModel } from '../model'
import { LabKeyQueryRowPropertyProps } from '../../../query/model'

const styles = require<any>('./PackageSearch.css');

interface PackageSearchInputProps {
    inputRenderer?: React.ComponentClass<{}>
    showInput?: boolean
}

export class PackageSearchInput extends React.Component<PackageSearchInputProps, {}> {

    private inputRenderer: React.ComponentClass<{}> = null;

    static defaultProps = {
        showInput: true
    };

    constructor(props?: PackageSearchInputProps) {
        super(props);

        const { inputRenderer } = props;

        if (inputRenderer) {
            this.inputRenderer = inputRenderer;
        }
        else {
            this.inputRenderer = DefaultInput
        }
    }

    render() {
        const { showInput } = this.props;

        if (showInput) {
            return React.createElement(this.inputRenderer);
        }

        return null;
    }
}

export interface PackageSearchRowProps {
    data: {[key: string]: LabKeyQueryRowPropertyProps}
    dataId: number
}

interface PackageSearchResultsProps {
    data: {[key: string]: LabKeyQueryRowPropertyProps}
    dataIds: Array<number>
    isLoaded: boolean
    rowRenderer?: React.ComponentClass<any> // todo: fix renderer typing
}

export class PackageSearchResults extends React.Component<PackageSearchResultsProps, any> {

    private rowRenderer: React.ComponentClass<any> = null;

    static defaultProps: PackageSearchResultsProps = {
        data: {},
        dataIds: [],
        isLoaded: false
    };

    constructor(props?: PackageSearchResultsProps) {
        super(props);

        const { rowRenderer } = props;

        if (rowRenderer) {
            this.rowRenderer = rowRenderer;
        }
        else {
            this.rowRenderer = DefaultRowRenderer
        }
    }

    render() {
        const { data, dataIds, isLoaded } = this.props;

        if (isLoaded && data && dataIds.length) {
            return (
                <div className="data-search__container">
                    {dataIds.map((d, i) => {
                        const rowData = data[d];
                        return (
                            <div key={'data-search__row' + i}>
                                {React.createElement(this.rowRenderer, {data: rowData, dataId: d})}
                            </div>
                        )
                    })}
                </div>
            )
        }
        else if (isLoaded && dataIds.length === 0) {
            return (
                <div className="data-search__container">
                    <div className="data-search__row">
                        No results found
                    </div>
                </div>
            )
        }

        return null;
    }
}


export class DefaultInput extends React.Component<any, any> {
    render() {
        return(
            <div>

            </div>
        )
    }
}




export class DefaultRowRenderer extends React.Component<any, any> {
    render() {
        return(
            <div>

            </div>
        )
    }
}

interface PackageViewerInputProps {
    dispatch?: Dispatch<{}>
    model: PackagesModel
}


interface PackageViewerInputStateProps {
    input?: string
}

function mapStateToProps(state: APP_STATE_PROPS): PackageViewerInputProps {

    return {
        model: state.packages
    }
}

export class PackageViewerInputImpl extends React.Component<PackageViewerInputProps, PackageViewerInputStateProps> {

    private timer: number = 0;
    private inputRef: HTMLInputElement;

    constructor(props: PackageViewerInputProps) {
        super(props);

        this.state = {
            input: ''
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

    toggleDrafts(evt: React.MouseEvent<any>) {
        const { dispatch, model } = this.props;

        dispatch(model.toggleDrafts())
    }

    render() {
        const { input } = this.state;
        const { showDrafts } = this.props.model;
        // todo: add css style sheet and webpack support

        return(
            <div className="package-viewer__header clearfix" style={{paddingBottom: '20px', paddingTop: '10px'}}>
                <div className={"col-sm-3 col-md-2 " + styles['packages-button']} >
                    <Link to="/packages/new">
                        <Button className={styles["packages-new_pkg_btn"]}>New Package</Button>
                    </Link>
                </div>
                <div className="col-sm-6 col-md-8" style={{position: 'relative'}}>
                    <i className={"fa fa-search " + styles['packages-icon']}/>
                    <i className={"fa fa-times-circle " + styles['packages-clear']} onClick={() => this.handleClear()}/>
                    <input
                        name="packageSearch"
                        onChange={(evt) => this.handleInputChange(evt)}
                        ref={(el) => this.inputRef = el}
                        className={styles["packages-search"]}
                        type="text"
                        value={input}/>
                </div>
                <div className="col-sm-3 col-md-2 packages-button-style" >
                    <div className={styles["packages-options"]}>
                        <DropdownButton id="package-actions" title="Options" style={{width: '105px'}} pullRight={true}>
                            <MenuItem>Edit Categories</MenuItem>
                            <MenuItem>Edit Projects</MenuItem>
                        </DropdownButton>
                    </div>
                </div>
                <div className={"col-xs-12 " + styles["packages-show_drafts"]} onClick={(evt) => this.toggleDrafts(evt)}>
                    <input type="checkbox" checked={showDrafts} readOnly/> Show drafts
                </div>
            </div>
        )
    }
}

export const PackageViewerInput = connect<any, any, any>(mapStateToProps)(PackageViewerInputImpl);