import React from 'react'

import './TopNavigation.css'
import view from '../logic/view'

const TopNavigation = React.createClass({
    render: function() {
        return (<div className="topNavigation">
            {this.item('KutsuList', 'NAYTA_KUTSUTUT_LINKKI')}
        </div>);
    },
    
    item: function(viewId, key) {
        const L = this.props.l10n;
        const currentView = this.props.view.length ? this.props.view[this.props.view.length-1] : '';
        if (currentView === viewId) {
            return (<div className="navigationItem currentView">{L[key]}</div>);
        }
        return (<a className="navigationItem" onClick={this.changeViewAction(viewId)}>{L[key]}</a>);
    },
    
    changeViewAction: function(to) {
        return () => {
            view.change(to);
        };
    }
});

export default TopNavigation;