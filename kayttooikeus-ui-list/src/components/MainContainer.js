import React from 'react'

import './MainContainer.css'

import AnomusListView from './AnomusListView.js'
import KutsuListView from './KutsuListView.js'

const MainContainer = React.createClass({
    render: function() {
        const uiLang = 'fi';
        const L = this.props.l10n[uiLang];
        const view = this.props.view.length ? this.props.view[this.props.view.length-1] : "";
        const props = {...this.props, uiLang, l10n: L};
        return (<div className="mainContainer">
            {this.renderView(props, view)}
        </div>);
    },
    
    renderView: function(props, view) {
        if (!view || view == 'AnomusList') {
            return (<AnomusListView {...props}></AnomusListView>);
        }
        if (view == 'KutsuList') {
            return (<KutsuListView {...props}></KutsuListView>);
        }
        return (<span>View {view} not found.</span>)
    }
});

export default MainContainer