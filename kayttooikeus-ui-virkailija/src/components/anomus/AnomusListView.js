import React from 'react'
import Bacon from 'baconjs'

import './AnomusListView.css'

import {l10nP} from '../../external/l10n'

const AnomusListView = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
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

export const anomusListViewContentP = Bacon.combineWith(l10nP, (l10n) => {
    const props = {l10n};
    return {
        content: <AnomusListView {...props}/>
    };
});

export default AnomusListView
