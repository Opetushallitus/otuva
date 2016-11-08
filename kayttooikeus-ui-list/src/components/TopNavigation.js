import React from 'react'

import './TopNavigation.css'
import view from '../logic/view'

const TopNavigation = React.createClass({
    render: function() {
        const L = this.props.l10n;
        return (<div className="topNavigation">
            <a className="navigationItem" onClick={this.changeViewAction('KutsuList')}>{L['NAYTA_KUTSUTUT_LINKKI']}</a>
        </div>);
    },
    
    changeViewAction: function(to) {
        return () => {
            view.change(to);
        };
    }
});

export default TopNavigation;