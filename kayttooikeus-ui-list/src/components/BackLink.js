import React from 'react'

import './BackLink.css'

const BackLink = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (<div className="backLink">
            <span>&lt;--</span>
            <a className="navigationItem" onClick={this.goBack}>{L['TAKAISIN_LINKKI']}</a>
        </div>);
    },
    
    goBack: function() {
        setTimeout(() => window.history.back(), 0);
    }
});

export default BackLink;