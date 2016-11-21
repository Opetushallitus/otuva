import React from 'react'

import './TopNavigation.css'
import {navigateTo} from '../logic/location'

const TopNavigation = React.createClass({
    render: function() {
        return (<div className="topNavigation">
            {this.item('/', 'NAYTA_ANOMUKSET_LINKKI')},
            {this.item('/kutsu/list', 'NAYTA_KUTSUTUT_LINKKI')},
            {this.item('/kutsu', 'VIRKAILIJAN_KUTSU_LINKKI')}
        </div>);
    },
    
    item: function(path, key) {
        const L = this.props.l10n;
        if (this.props.location.path === path || (!this.props.location.path && path === '/')) {
            return (<span className="navigationItem currentView">{L[key]}</span>);
        }
        return (<a className="navigationItem" onClick={this.changeViewAction(path)}>{L[key]}</a>);
    },
    
    changeViewAction: function(to) {
        return () => {
            navigateTo(to);
        };
    }
});

export default TopNavigation;