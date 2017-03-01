import './HenkiloViewOrganisationContent.css'
import React from 'react'
import Columns from 'react-columns'
import Button from "button";
import {updateHenkilo} from "../../external/henkiloClient";

const HenkiloViewOrganisationContent = React.createClass({
    propTypes: {
        l10n: React.PropTypes.object.isRequired,
        henkilo: React.PropTypes.object.isRequired,
        readOnly: React.PropTypes.bool.isRequired,
        showPassive: React.PropTypes.bool,
        locale: React.PropTypes.string.isRequired,
    },
    getInitialState: function() {
        const organisations = this.props.organisations.map(organisation => organisation.result);
        this.henkiloUpdate = this.props.henkilo.result;

        return {
            readOnly: this.props.readOnly,
            showPassive: false,
            organisationInfo: organisations.map(organisation =>
                ({name: organisation.nimi[this.props.locale], typesFlat: organisation.tyypit && organisation.tyypit.reduce((type1, type2) => type1.concat(', ', type2)),
                    role: organisation.orgHenkilo.tehtavanimike, passive: organisation.orgHenkilo.passivoitu})),
        }
    },
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="henkiloViewUserContentWrapper">
                <Columns columns={1}>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_ORGANISAATIOT_OTSIKKO']}</h2>
                        </div>
                        <input type="checkbox" onChange={() => this.setState({showPassive: !this.state.showPassive})} />
                        <span> {L['HENKILO_NAYTA_PASSIIVISET_TEKSTI']}</span>
                        <div className="henkiloViewContent">
                            {this.state.organisationInfo.map((values, idx) =>
                                !values.passive || this.state.showPassive
                                    ? <div key={idx}>
                                        <div><span className="strong">{values.name} ({values.typesFlat})</span></div>
                                        <div>
                                            <span className="strong">{L['HENKILO_TEHTAVANIMIKE']}:</span>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                            <span>{values.role}</span>
                                        </div>
                                        {!values.passive
                                            ? <div><Button action={() => {}}>{L['HENKILO_PASSIVOI']}</Button></div>
                                            : null}
                                    </div>
                                    : null
                            )}
                        </div>
                    </div>
                </Columns>
            </div>
        )
    },
});

export default HenkiloViewOrganisationContent
