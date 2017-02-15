import React from 'react'

import './Field.css';
import classNames from 'classnames/bind';

const Field = React.createClass({
    propTypes: {
        readOnly: React.PropTypes.bool,
        changeAction: React.PropTypes.func,
        inputValue: React.PropTypes.string
    },
    getInitialState: function () {
        return {
            readOnly: true
        }
    },
    render: function() {
        const className = classNames({'field': true,
            '${this.props.className}': this.props.className,
            'readonly': this.props.readOnly});
        return (
            this.props.readOnly
                ? <span className={className}>{this.props.children}</span>
                : <input className={className} name={this.props.inputValue} onChange={this.props.changeAction}
                         defaultValue={this.props.children} />
        )
    }
});
export default Field;
