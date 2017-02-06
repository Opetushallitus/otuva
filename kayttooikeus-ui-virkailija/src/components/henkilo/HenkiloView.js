import './HenkiloView.css'
import React from 'react'
import Bacon from 'baconjs'
import HenkiloViewUserContent from './HenkiloViewUserContent'

import {l10nP} from '../../external/l10n'
import {henkiloNavi} from "../../external/navilists";
import {locationP} from "../../logic/location";

const HenkiloView = React.createClass({
    getInitialState: function() {
        return {
        }
    },
    componentWillMount: function () {
        this.originalBackgroundColor = document.body.style.backgroundColor;
        document.body.style.backgroundColor = "#f6f4f0";
    },
    componentWillUnmount: function () {
        document.body.style.backgroundColor = this.originalBackgroundColor;
    },
    render: function() {
        return (
        <div>
            <div className="wrapper">
                <HenkiloViewUserContent {...this.props} readOnly={true} />
            </div>
            <div className="wrapper">Another</div>
        </div>
        )
    }
});

export const henkiloViewContentP = Bacon.combineWith(l10nP, locationP, (l10n, location) => {
    const props = {l10n};
    henkiloNavi.oid = location.params['oid'];
    henkiloNavi.backLocation = '/henkilo';
    return {
        content: <HenkiloView {...props}/>,
        navi: henkiloNavi
    };
});

export default HenkiloView
