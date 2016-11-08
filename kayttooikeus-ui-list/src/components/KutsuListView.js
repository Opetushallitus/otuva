import React from 'react'

import './KutsuListView.css'

import BackLink from './BackLink'

const KutsuListView = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
                <BackLink {...this.props}></BackLink>
                <div className="header">
                    <h1>{L['KUTSUTUT_VIRKAILIJAT_OTSIKKO']}</h1>
                </div>
            </div>
        )
    }
});

export default KutsuListView
