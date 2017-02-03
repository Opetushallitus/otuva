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
            readOnly: true
        }
    },
    render: function() {
        return (
            <div className="wrapper">
                <form>
                    <HenkiloViewUserContent {...this.props} />
                </form>
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
