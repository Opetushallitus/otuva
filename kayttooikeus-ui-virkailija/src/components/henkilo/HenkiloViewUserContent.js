import './HenkiloViewUserContent.css'
import React from 'react'
import Columns from 'react-columns'
import Field from 'field';
import Button from "button";

const HenkiloViewUserContent = React.createClass({
    getInitialState: function() {
        return {
            readOnly: this.props.readOnly,
            showPassive: false
        }
    },
    render: function() {
        const L = this.props.l10n;
        const basicInfo = [
            {translation: 'HENKILO_ETUNIMET', value: this.props.etunimet},
            {translation: 'HENKILO_SUKUNIMI', value:  this.props.sukunimi},
            {translation: 'HENKILO_SYNTYMAAIKA', value:  this.props.syntymaaika},
            this.props.kansalaisuus && this.props.kansalaisuus.length
                ? this.props.kansalaisuus.map(values => ({translation: 'HENKILO_KANSALAISUUS', value: values.kansalaisuusKoodi}))
                : {translation: 'HENKILO_KANSALAISUUS', value: null},
            {translation: 'HENKILO_AIDINKIELI', value:  this.props.aidinkieli && this.props.aidinkieli.kieliTyyppi},
            {translation: 'HENKILO_KAYTTAJANIMI', value:  this.props.kayttajanimi},
            {translation: 'HENKILO_ASIOINTIKIELI', value:  this.props.asiointiKieli && this.props.asiointiKieli.kieliTyyppi},
        ];
        const contactInfo = this.props.yhteystiedotRyhma.map(yhteystiedotRyhma =>
            yhteystiedotRyhma.yhteystieto.map(yhteystieto =>
                ({translation: yhteystieto.yhteystietoTyyppi, value: yhteystieto.yhteystietoArvo})
            )
        ).reduce((a,b) => a.concat(b));
        const organisationInfo = this.props.organisations.map(organisation =>
            ({name: organisation.organisaatio.nimi.fi, typesFlat: organisation.organisaatio.tyypit.reduce((type1, type2) => type1.concat(', ', type2)),
            role: organisation.tehtavanimike, passive: organisation.passivoitu}));
        return (
            <div className="henkiloViewUserContentWrapper">
                <Columns columns={3}>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_PERUSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent">
                            {basicInfo.map((values, idx) =>
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
                            {contactInfo.map((values, idx) =>
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
                            {organisationInfo.map((values, idx) =>
                                !values.passive || this.state.showPassive
                                    ? <div key={idx}>
                                        <div><span>{values.name} ({values.typesFlat})</span></div>
                                        <div><span>{L['HENKILO_TEHTAVANIMIKE']}: {values.role}</span></div>
                                        <div><Button action={() => {}}>{L['HENKILO_PASSIVOI']}</Button></div>
                                    </div>
                                    : null
                            )}
                        </div>
                    </div>
                </Columns>
                {this.state.readOnly
                    ? <div className="henkiloViewButtons">
                        <Button action={() => this.setState({readOnly: false})}>{L['MUOKKAA_LINKKI']}</Button>
                        <Button action={() => {}}>{L['YKSILOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['PASSIVOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['LISAA_HAKA_LINKKI']}</Button>
                    </div>
                    : <div className="henkiloViewEditButtons">
                        <Button action={() => {}}>{L['TALLENNA_LINKKI']}</Button>
                        <Button action={() => {}}>{L['PERUUTA_LINKKI']}</Button>
                    </div> }
            </div>
        )
    }
});

HenkiloViewUserContent.propTypes = {
    l10n: React.PropTypes.object.isRequired
};

export default HenkiloViewUserContent
