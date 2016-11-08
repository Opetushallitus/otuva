import React from 'react'

import './BackLink.css'

import view from '../logic/view'

const BackLink = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (<div className="backLink">
            <span>&lt;--</span>
            <a className="navigationItem" onClick={view.back}>{L['TAKAISIN_LINKKI']}</a>
        </div>);
    }
});

export default BackLink;