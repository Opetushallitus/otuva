import React from 'react'

import './TopNavigation.css'
import {navigateTo} from '../logic/location'

const TopNavigation = React.createClass({
    render: function() {
        return (<ul className="tabs">
            { this.props.items.map(this.item)}
        </ul>);
    },
    
    item: function(value, idx, array) {
        let path = value.path;
        let key =  value.value;
        if(array.oid) {
            path += '?oid=' + array.oid;
        }
        const L = this.props.l10n;
        if (this.props.location.path === value.path || (!this.props.location.path && value.path === '/')) {
            return (<li key={idx} className="active">{L[key]}</li>);
        }
        return (<li key={idx} onClick={this.changeViewAction(path)}>{L[key]}</li>);
    },
    
    changeViewAction: function(to) {
        return () => {
            navigateTo(to);
        };
    }
});

TopNavigation.propTypes = {
    items: React.PropTypes.arrayOf(React.PropTypes.object)
};

export default TopNavigation;