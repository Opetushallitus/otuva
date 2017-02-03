import React from 'react'

import './TopNavigation.css'
import {navigateTo} from '../logic/location'
import Button from "button";

const TopNavigation = React.createClass({
    render: function() {
        const L = this.props.l10n;
        const s = this.props.items.backLocation
            ? <Button action={this.changeViewAction(this.props.items.backLocation)}>&#8701; {L['TAKAISIN_LINKKI']}</Button>
            : null;
        return (
            <div>
                {s}
                <ul className="tabs">{ this.props.items.map(this.item)}</ul>
            </div>
        )
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