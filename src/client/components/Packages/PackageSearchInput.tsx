import * as React from 'react';

import { Button, DropdownButton, MenuItem } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import { SearchInput } from "../Search/SearchInput";

const styles = require<any>('../../containers/Packages/Forms/PackageSearch.css');

interface PackageSearchInputProps {
    handleClear: () => any
    handleInputChange?: (input) => any
    input?: string
    inputRef: any
    showDrafts: boolean
    toggleDrafts: () => any
}

export class PackageSearchInput extends React.Component<PackageSearchInputProps, {}> {

    render() {
        const {
            handleClear,
            handleInputChange,
            input,
            inputRef,
            showDrafts,
            toggleDrafts
        } = this.props;

        return(
            <div>
                <div className="package-viewer__header clearfix" style={{paddingBottom: '20px', paddingTop: '10px'}}>
                    <div className={"col-sm-3 col-md-2 " + styles['packages-button']}>
                        <Link to="/packages/new">
                            <Button className={styles['packages-new_pkg_btn']}>New Package</Button>
                        </Link>
                    </div>
                    <SearchInput
                        className={"col-sm-6 col-md-8"}
                        inputRef={inputRef}
                        input={input}
                        handleInputChange={handleInputChange}
                        handleClear={handleClear}
                    />
                    <div className={"col-sm-3 col-md-2 " + styles['packages-button']}>
                        <div className={styles['packages-options']}>
                            <DropdownButton id="package-actions" title="Options" pullRight style={{width: '105px'}}>
                                <MenuItem>Edit Categories</MenuItem>
                                <MenuItem>Edit Projects</MenuItem>
                            </DropdownButton>
                        </div>
                    </div>
                    <div className={"col-xs-12 " + styles['packages-show_drafts']} onClick={toggleDrafts}>
                        <input type="checkbox" checked={showDrafts}/> Show drafts
                    </div>
                </div>
                <div style={{borderBottom: '1px solid black', margin: '0 15px'}}/>
            </div>
        )
    }
}