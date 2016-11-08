import React from 'react'

import './KutsuListView.css'

const KutsuListView = React.createClass({

    getInitialState: function() {
        return {
        }
    },

    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
                <div className="header">
                    <h1>{L['KUTSUTUT_VIRKAILIJAT_OTSIKKO']}</h1>
                </div>
                
            </div>
        )
    }
});

export default KutsuListView
