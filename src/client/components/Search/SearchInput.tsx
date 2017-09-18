import * as React from 'react';

const styles = require<any>('./SearchInput.css');

interface SearchInputProps {
    className: string
    handleClear: () => any
    handleInputChange?: (input) => any
    input?: string
    inputRef: any
}


export class SearchInput extends React.Component<SearchInputProps, {}> {

    render() {
        const { className, handleClear, handleInputChange, input, inputRef} = this.props;

        return (
            <div className={className}>
                <i className={"fa fa-search " + styles['searchinput-icon']}/>
                <i className={"fa fa-times-circle " + styles['searchinput-clear']} onClick={handleClear}/>
                <input
                    className={styles['searchinput-search']}
                    name="packageSearch"
                    onChange={handleInputChange}
                    ref={inputRef}
                    type="text"
                    value={input}/>
            </div>
        );
    }
}