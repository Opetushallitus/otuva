import React from 'react'

import './Button.css';

const Button = React.createClass({
    propTypes: {
        action: React.PropTypes.func.isRequired
    },
    
    render: function() {
        return (<span onClick={this.props.action} className="button">{this.props.children}</span>);
    }
});
export default Button;