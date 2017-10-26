import * as React from 'react';

const styles = require<any>('./SearchInput.css');

interface SearchInputProps {
    allowClear?: boolean
    allowToggle?: boolean
    disabled?: boolean
    handleClear?: any
    handleFocus?: (el: React.FocusEvent<HTMLInputElement>) => void
    handleInputChange?: (input) => any
    handleToggle?: () => void
    input?: string
    inputClassName?: string
    inputRef?: any
    name: string
    toggled?: boolean
    wrapperClassName?: string
}


export class SearchInput extends React.Component<SearchInputProps, {}> {

    static defaultProps = {
        allowClear: true,
        allowToggle: false,
        disabled: false,
        toggled: false,
        wrapperClassName: 'col-sm-6 col-sm-8',
    };

    handleFocus(el: React.FocusEvent<HTMLInputElement>) {
        const { handleFocus } = this.props;
        if (handleFocus && typeof handleFocus === 'function') {
            handleFocus(el);
        }

    }

    handleToggle() {
        const { handleToggle } = this.props;
        if (handleToggle && typeof handleToggle === 'function') {
            handleToggle();
        }
    }

    renderToggle() {
        const { allowToggle, toggled } = this.props;

        if (allowToggle) {
            let classes = [styles['searchinput-caret']];
            if (toggled) {
                classes.push("fa fa-caret-down");
            }
            else if (!toggled) {
                classes.push("fa fa-caret-right")
            }
            return <i className={classes.join(' ')} onClick={() => this.handleToggle()}/>;
        }

        return null;
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
                {this.renderToggle()}
                <input
                    className={[styles['searchinput-search'], inputClassName].join(' ')}
                    disabled={disabled}
                    name={name}
                    onChange={handleInputChange}
                    onFocus={(el: React.FocusEvent<HTMLInputElement>) => this.handleFocus(el)}
                    ref={inputRef}
                    type="text"
                    value={input ? input : ''}/>
            </div>
        );
    }
}