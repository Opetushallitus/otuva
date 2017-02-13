import './HenkiloViewUserContent.css'
import React from 'react'
import Columns from 'react-columns'
import Field from 'field';
import Button from "button";

const HenkiloViewUserContent = React.createClass({
    getInitialState: function() {
        return {
            readOnly: this.props.readOnly,
            showPassive: false,
            basicInfo: [
                {translation: 'HENKILO_ETUNIMET', value: this.props.etunimet},
                {translation: 'HENKILO_SUKUNIMI', value:  this.props.sukunimi},
                {translation: 'HENKILO_SYNTYMAAIKA', value:  this.props.syntymaaika},
                this.props.kansalaisuus && this.props.kansalaisuus.length
                    ? this.props.kansalaisuus.map(values => ({translation: 'HENKILO_KANSALAISUUS', value: values.kansalaisuusKoodi}))
                    : {translation: 'HENKILO_KANSALAISUUS', value: null},
                {translation: 'HENKILO_AIDINKIELI', value:  this.props.aidinkieli && this.props.aidinkieli.kieliTyyppi},
                {translation: 'HENKILO_KAYTTAJANIMI', value:  this.props.kayttajanimi},
                {translation: 'HENKILO_ASIOINTIKIELI', value:  this.props.asiointiKieli && this.props.asiointiKieli.kieliTyyppi},
            ],
            contactInfo: this.props.yhteystiedotRyhma.map(yhteystiedotRyhma =>
                yhteystiedotRyhma.yhteystieto.map(yhteystieto =>
                    ({translation: yhteystieto.yhteystietoTyyppi, value: yhteystieto.yhteystietoArvo})
                )
            ).reduce((a,b) => a.concat(b)),
            organisationInfo: this.props.organisations.map(organisation =>
                ({name: organisation.organisaatio.nimi.fi, typesFlat: organisation.organisaatio.tyypit.reduce((type1, type2) => type1.concat(', ', type2)),
                    role: organisation.tehtavanimike, passive: organisation.passivoitu}))
        }
    },
    propTypes: function () {
        return {
            l10n: React.PropTypes.object.isRequired
        }
    },
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="henkiloViewUserContentWrapper">
                <Columns columns={3}>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_PERUSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent">
                            {this.state.basicInfo.map((values, idx) =>
                            <Columns columns={2}  key={idx}>
                                <span>{L[values.translation]} </span>
                                <Field readOnly={this.state.readOnly}>{values.value}</Field>
                            </Columns>
                            )}
                        </div>
                    </div>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_YHTEYSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent">
                            {this.state.contactInfo.map((values, idx) =>
                                <Columns columns={2} key={idx}>
                                    <span>{L[values.translation]} </span>
                                    <Field readOnly={this.state.readOnly}>{values.value}</Field>
                                </Columns>
                            )}
                        </div>
                    </div>
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
                                        <div><span>{values.name} ({values.typesFlat})</span></div>
                                        <div><span>{L['HENKILO_TEHTAVANIMIKE']}: {values.role}</span></div>
                                        {!this.state.readOnly
                                            ? <div><Button action={() => {}}>{L['HENKILO_PASSIVOI']}</Button></div>
                                            : null}
                                    </div>
                                    : null
                            )}
                        </div>
                    </div>
                </Columns>
                {this.state.readOnly
                    ? <div className="henkiloViewButtons">
                        <Button action={this._edit}>{L['MUOKKAA_LINKKI']}</Button>
                        <Button action={() => {}}>{L['YKSILOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['PASSIVOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['LISAA_HAKA_LINKKI']}</Button>
                    </div>
                    : <div className="henkiloViewEditButtons right">
                        <Button action={() => {}}>{L['TALLENNA_LINKKI']}</Button>
                        <Button action={this._discard}>{L['PERUUTA_LINKKI']}</Button>
                    </div>
                }
            </div>
        )
    },
    _edit: function () {
        this.setState({readOnly: false});
        this._preEditData = {
            basicInfo: this.state.basicInfo,
            contactInfo: this.state.contactInfo,
            organisationInfo: this.state.organisationInfo,
        }
    },
    _discard: function () {
        this.setState({
            readOnly: true,
            basicInfo: this._preEditData.basicInfo,
            contactInfo: this._preEditData.contactInfo,
            organisationInfo: this._preEditData.organisationInfo,
        });
    }
});

export default HenkiloViewUserContent
