import React from 'react'

import './Button.css';

const Button = React.createClass({
    propTypes: {
        action: React.PropTypes.func.isRequired,
        disabled: React.PropTypes.bool
    },
    
    render: function() {
        const className = "button " + (this.props.className ? " " + this.props.className : "")
                    + (this.props.disabled ? " disabled":"");
        return (<span className={className} onClick={!this.props.disabled ? this.props.action : () => {}}>{this.props.children}</span>);
    }
});
export default Button;