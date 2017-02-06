import React from 'react'
import classNames from 'classnames/bind';

import './Button.css';

const Button = React.createClass({
    propTypes: {
        action: React.PropTypes.func,
        disabled: React.PropTypes.bool,
        href: React.PropTypes.string
    },
    
    render: function() {
        const className = classNames({
            'button': true,
            'disabled': this.props.disabled,
            '${this.props.className}': this.props.className
        });
        return (
            this.props.href
                ? <a href={this.props.href} className="a" onClick={this.props.action}>{this.props.children}</a>
                : <span className={className} onClick={!this.props.disabled ? this.props.action : () => {}}>{this.props.children}</span>
        );
    }
});

export default Button;
