import React from 'react'

import './TopNavigation.css'
import {navigateTo} from '../logic/location'

const TopNavigation = React.createClass({
    render: function() {
        return (<ul className="tabs">
            {this.item('/', 'NAYTA_ANOMUKSET_LINKKI')}
            {this.item('/kutsu/list', 'NAYTA_KUTSUTUT_LINKKI')}
            {this.item('/kutsu', 'VIRKAILIJAN_LISAYS_OTSIKKO')}
        </ul>);
    },
    
    item: function(path, key) {
        const L = this.props.l10n;
        if (this.props.location.path === path || (!this.props.location.path && path === '/')) {
            return (<li className="active">{L[key]}</li>);
        }
        return (<li onClick={this.changeViewAction(path)}>{L[key]}</li>);
    },
    
    changeViewAction: function(to) {
        return () => {
            navigateTo(to);
        };
    }
});

export default TopNavigation;