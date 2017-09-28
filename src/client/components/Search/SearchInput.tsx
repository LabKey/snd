import * as React from 'react';

const styles = require<any>('./SearchInput.css');

interface SearchInputProps {
    allowClear?: boolean
    disabled?: boolean
    handleClear?: any
    handleFocus?: (el: React.FocusEvent<HTMLInputElement>) => void
    handleInputChange?: (input) => any
    input?: string
    inputRef?: any
    name: string
    wrapperClassName?: string
    inputClassName?: string
}


export class SearchInput extends React.Component<SearchInputProps, {}> {

    static defaultProps = {
        allowClear: true,
        wrapperClassName: 'col-sm-6 col-sm-8',
        disabled: false
    };

    handleFocus(el: React.FocusEvent<HTMLInputElement>) {
        const { handleFocus } = this.props;
        if (handleFocus && typeof handleFocus === 'function') {
            handleFocus(el);
        }

    }

    render() {
        const {
            allowClear,
            disabled,
            handleClear,
            handleInputChange,
            input,
            inputClassName,
            inputRef,
            name,
            wrapperClassName
        } = this.props;

        return (
            <div className={wrapperClassName}>
                <i className={"fa fa-search " + styles['searchinput-icon']}/>
                {allowClear && input !== '' ?
                    <i className={"fa fa-times-circle " + styles['searchinput-clear']} onClick={handleClear}/>
                : null}
                <input
                    className={[styles['searchinput-search'], inputClassName].join(' ')}
                    disabled={disabled}
                    name={name}
                    onChange={handleInputChange}
                    onFocus={(el: React.FocusEvent<HTMLInputElement>) => this.handleFocus(el)}
                    ref={inputRef}
                    type="text"
                    value={input}/>
            </div>
        );
    }
}