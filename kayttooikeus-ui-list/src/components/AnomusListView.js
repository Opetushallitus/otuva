import React from 'react'

import './AnomusListView.css'

import TopNavigation from './TopNavigation'

const AnomusListView = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
                <TopNavigation {...this.props}/>
                <div className="header">
                    <h1>{L['HYVAKSYMATTOMAT_KAYTTOOIKEUSANOMUKSET_OTSIKKO']}</h1>
                </div>
                <p>
                    TODO: taulukko tähän (oma tikettinsä)
                </p>
            </div>
        )
    }
});

export default AnomusListView
