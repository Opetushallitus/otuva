import React from 'react'
import Bacon from 'baconjs'
import Button from "button";

import './HenkiloListView.css'

import {l10nP} from '../../external/l10n'
import {defaultNavi} from "../../external/navilists";
import {navigateTo} from "../../logic/location";

const HenkiloListView = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="wrapper">
                <div className="header">
                    <h2>{L['HENKILO_HAKU_OTSIKKO']}</h2>
                </div>
                <p>
                    TODO: taulukko tähän (oma tikettinsä)
                    <Button action={() => navigateTo('/henkilo?oid=1.234.567.8910111213')}>Testihenkilöön</Button>
                </p>
            </div>
        )
    }
});

export const henkiloListViewContentP = Bacon.combineWith(l10nP, (l10n) => {
    const props = {l10n};
    return {
        content: <HenkiloListView {...props}/>,
        navi: defaultNavi
    };
});

export default HenkiloListView
