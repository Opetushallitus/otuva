import React from 'react'
import Bacon from 'baconjs'
import dateformat from 'dateformat'

import Modal from 'modal'
import Button from 'button'
import SortByHeader from 'sort-by-header'

import BackLink from '../BackLink'

import kutsuList, { kutsuListStateP, kutsuListP, peruutaKutsu } from '../../external/kutsu'
import { l10nP, localeP } from '../../external/l10n'
import { setSuccess } from '../../logic/error'

import './KutsuListView.css'

const KutsuListView = React.createClass({
    getInitialState: function() {
        return {
            confirmDeleteFor: null
        };
    },
    
    render: function() {
        const L = this.props.l10n;
        const kutsuResponse = this.props.kutsuList;
        const state = this.props.kutsuListState.params;
        return (
            <div className="wrapper">
                <BackLink {...this.props}/>
                <div className="header">
                    <h2>{L['KUTSUTUT_VIRKAILIJAT_OTSIKKO']}</h2>
                </div>
                {!kutsuResponse.loaded 
                    && <div className="loading">{L['LADATAAN']}
                    </div>}
                {kutsuResponse.loaded && !kutsuResponse.result.length > 0
                    && <div className="noResults">{L['EI_KUTSUJA']}</div>}
                {kutsuResponse.loaded && kutsuResponse.result.length > 0
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
                                {kutsuResponse.result.map(kutsu => <tr key={kutsu.id}>
                                    <td>
                                        {kutsu.sahkoposti}
                                    </td>
                                    <td>
                                        {kutsu.organisaatiot.map(org => 
                                            <div className="kutsuOrganisaatio" key={org.oid}>{org.nimi[this.props.locale]}</div>)}
                                    </td>
                                    <td>
                                        {dateformat(new Date(kutsu.aikaleima), L['PVM_FORMAATTI'])}
                                    </td>
                                    <th>
                                        {kutsu.tila === 'AVOIN' && <Button className="cancel" action={this.cancelInvitationAction(kutsu)}>{L['PERUUTA_KUTSU']}</Button>}
                                    </th>
                                </tr>)}
                            </tbody>
                        </table>
                    </div>}
                
                {this.state.confirmDeleteFor != null && <Modal show={this.state.confirmDeleteFor != null} onClose={this.cancelInvitationCancellation}
                            closeOnOuterClick={true}>
                    <div className="confirmation-modal">
                        <h2>{L['PERUUTA_KUTSU_VAHVISTUS']}</h2>
                        <table>
                            <tbody>
                                <tr>
                                    <th>{L['KUTSUT_SAHKOPOSTI_OTSIKKO']}</th>
                                    <td>{this.state.confirmDeleteFor.sahkoposti}</td>
                                </tr>
                                <tr>
                                    <th>{L['KUTSUTUT_ORGANISAATIO_OTSIKKO']}</th>
                                    <td>{this.state.confirmDeleteFor.organisaatiot.map(org =>
                                        <div className="kutsuOrganisaatio" key={org.oid}>{org.nimi[this.props.locale]}</div>)}</td>
                                </tr>
                                <tr>
                                    <th>{L['KUTSUTUT_KUTSU_LAHETETTY_OTSIKKO']}</th>
                                    <td>{dateformat(new Date(this.state.confirmDeleteFor.aikaleima), L['PVM_FORMAATTI'])}</td>
                                </tr>
                            </tbody>
                        </table>
                        <div className="row">
                            <Button className="left action" action={this.cancelInvitationConfirmed}>
                                {L['PERUUTA_KUTSU']}
                            </Button>
                            <Button className="right cancel" action={this.cancelInvitationCancellation}>
                                {L['PERUUTA_KUTSUN_PERUUTTAMINEN']}
                            </Button>
                        </div>
                        <div className="clear"></div>
                    </div>
                </Modal>}
            </div>);
    },

    changeOrder: function(sortBy, direction) {
        kutsuList.order(sortBy, direction);
    },
    
    cancelInvitationAction: function(r) {
        return () => {
            this.setState({confirmDeleteFor: r});
        };
    },
    
    cancelInvitationCancellation: function() {
        this.setState({confirmDeleteFor: null});
    },

    cancelInvitationConfirmed: function() {
        if (this.state.confirmDeleteFor) {
            peruutaKutsu(this.state.confirmDeleteFor.id).onValue(() => {
                setSuccess('KUTSU_PERUUTETTU');
                this.setState({confirmDeleteFor: null});
            });
        }
    },

    componentDidMount: function() {
        kutsuList.activate(); // initial fetch
    }
});

export const kutsuListViewContentP = Bacon.combineWith(l10nP, localeP, kutsuListStateP, kutsuListP,
        (l10n, locale, kutsuListState, kutsuList) => {
    const props = {l10n, locale, kutsuListState, kutsuList};
    return {
        content: <KutsuListView {...props}/>
    };
});

export default KutsuListView
