import React from 'react'

import './AnomusListView.css'

const AnomusListView = React.createClass({
    getInitialState: function() {
        return {
        }
    },

    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
                <div className="header">
                    <h1>{L['HYVAKSYMATTOMAT_KAYTTOOIKEUSANOMUKSET_OTSIKKO']}</h1>
                </div>
                
            </div>
        )
    }
});

export default AnomusListView
