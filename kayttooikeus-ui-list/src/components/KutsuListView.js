import React from 'react'
import dateformat from 'dateformat'

import './KutsuListView.css'
import kutsuList from '../external/kutsuList'
import TopNavigation from './TopNavigation'
import Button from './Button'
import SortByHeader from './SortByHeader'

import BackLink from './BackLink'

const KutsuListView = React.createClass({
    render: function() {
        const L = this.props.l10n;
        const kutsuResponse = this.props.kutsuList;
        const state = this.props.kutsuListState.params;
        return (
            <div className="wrapper">
                <TopNavigation {...this.props}/>
                <BackLink {...this.props}/>
                <div className="header">
                    <h1>{L['KUTSUTUT_VIRKAILIJAT_OTSIKKO']}</h1>
                </div>
                {!kutsuResponse.loaded 
                    && <div className="loading">{L['LADATAAN']}</div>}
                {kutsuResponse.loaded && !kutsuResponse.result.length 
                    && <div className="noResults">{L['EI_KUTSUJA']}</div>}
                {kutsuResponse.loaded && kutsuResponse.result.length 
                    && <div className="results">
                        <table>
                            <thead>
                                <tr>
                                    <SortByHeader by="SAHKOPOSTI" state={state} onChange={this.changeOrder}>
                                        {L['KUTSUT_SAHKOPOSTI_OTSIKKO']}
                                    </SortByHeader>
                                    <SortByHeader by="ORGANISAATIO" state={state} onChange={this.changeOrder}>
                                        {L['KUTSUTUT_ORGANISAATIO_OTSIKKO']}
                                    </SortByHeader>
                                    <SortByHeader by="AIKALEIMA" state={state} onChange={this.changeOrder}>
                                        {L['KUTSUTUT_KUTSU_LAHETETTY_OTSIKKO']}
                                    </SortByHeader>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {kutsuResponse.result.map(r => <tr key={r.id}>
                                    <td>
                                        {r.sahkoposti}
                                    </td>
                                    <td>
                                        {r.organisaatiot.map(org => 
                                            <div className="kutsuOrganisaatio" key={org.oid}>{org.nimi[this.props.uiLang]}</div>)}
                                    </td>
                                    <td>
                                        {dateformat(new Date(r.aikaleima), L['PVM_FORMAATTI'])}
                                    </td>
                                    <th>
                                        {r.tila === 'AVOIN' && <Button action={this.cancelInvitationAction(r)}>{L['PERUUTA_KUTSU']}</Button>}
                                    </th>
                                </tr>)}
                            </tbody>
                        </table>
                    </div>}
            </div>
        )
    },

    changeOrder: function(sortBy, direction) {
        kutsuList.order(sortBy, direction);
    },
    
    cancelInvitationAction: function(r) {
        const L = this.props.l10n;
        return () => {
            if (confirm(L['PERUUTA_KUTSU_VAHVISTUS'])) {
                // TODO
            }
        };
    },

    componentDidMount: function() {
        kutsuList.activate();
    }
});

export default KutsuListView
