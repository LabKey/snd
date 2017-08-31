import * as React from 'react';
import { LabKeyQueryRowPropertyProps } from '../../../query/model'

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
    rowRenderer?: React.ComponentClass<any> // todo: fix renderer typing
}

export class PackageSearchResults extends React.Component<PackageSearchResultsProps, any> {

    private rowRenderer: React.ComponentClass<any> = null;


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
        const { data, dataIds } = this.props;

        if (data && dataIds.length) {
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

        return null;
    }
}


export class DefaultInput extends React.Component<any, any> {
    render() {
        console.log('default input', this.props)
        return(
            <div>

            </div>
        )
    }
}




export class DefaultRowRenderer extends React.Component<any, any> {
    render() {
        console.log('default row', this.props)
        return(
            <div>

            </div>
        )
    }
}
