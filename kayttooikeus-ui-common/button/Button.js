import React from 'react'

import './Button.css';

const Button = React.createClass({
    propTypes: {
        action: React.PropTypes.func.isRequired
    },
    
    render: function() {
        const className = "button " + (this.props.className ? " " + this.props.className : "");
        return (<span className={className} onClick={this.props.action}>{this.props.children}</span>);
    }
});
export default Button;