import './HenkiloViewUserContent.css'
import React from 'react'
import Columns from 'react-columns'
import Field from 'field';
import Button from "button";
import {updateHenkilo} from "../../external/henkiloClient";

const HenkiloViewUserContent = React.createClass({
    propTypes: function () {
        return {
            l10n: React.PropTypes.object.isRequired,
            henkilo: React.PropTypes.object.isRequired,
            readOnly: React.PropTypes.bool.isRequired,
            showPassive: React.PropTypes.bool,
            locale: React.PropTypes.string.isRequired,
        }
    },
    getInitialState: function() {
        const organisations = this.props.organisations.map(organisation => organisation.result);
        const kayttajatieto = this.props.kayttajatieto.result;
        this.henkiloUpdate = this.props.henkilo.result;
        this.kieliKoodis = this.props.koodistoKieli.result.map(koodi =>
            ({value: koodi.koodiArvo.toLowerCase(),
                ...koodi.metadata.map(kieliKoodi =>
                    ({[kieliKoodi.kieli.toLowerCase()]: kieliKoodi.nimi})).reduce((a,b) => {
                    a[Object.keys(b)[0]] = b[Object.keys(b)[0]];
                    return a
                }, {})
            })
        );
        this.kansalaisuusKoodis = this.props.koodistoKansalaisuus.result.map(koodi =>
            ({value: koodi.koodiArvo.toLowerCase(),
                ...koodi.metadata.map(kansalaisuusKoodi =>
                    ({[kansalaisuusKoodi.kieli.toLowerCase()]: kansalaisuusKoodi.nimi})).reduce((a,b) => {
                    a[Object.keys(b)[0]] = b[Object.keys(b)[0]];
                    return a
                }, {})
            })
        );

        return {
            readOnly: this.props.readOnly,
            showPassive: false,
            basicInfo: [
                {translation: 'HENKILO_ETUNIMET', value: this.henkiloUpdate.etunimet, inputValue: 'etunimet'},
                {translation: 'HENKILO_SUKUNIMI', value: this.henkiloUpdate.sukunimi, inputValue: 'sukunimi'},
                {translation: 'HENKILO_SYNTYMAAIKA', value: this.henkiloUpdate.syntymaaika, inputValue: 'syntymaaika'},
                this.henkiloUpdate.kansalaisuus && this.henkiloUpdate.kansalaisuus.length
                    ? this.henkiloUpdate.kansalaisuus.map((values, idx) => ({translation: 'HENKILO_KANSALAISUUS',
                        data: this.kansalaisuusKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                        value: this.kansalaisuusKoodis.filter(kansalaisuus => kansalaisuus.value === values.kansalaisuusKoodi)[0][this.props.locale], inputValue: 'kansalaisuus.' + idx + '.kansalaisuusKoodi',
                        selectValue: values.kansalaisuusKoodi
                    })).reduce((a,b) => a.concat(b))
                    : {translation: 'HENKILO_KANSALAISUUS',
                        data: this.kansalaisuusKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                        inputValue: 'kansalaisuus.0.kansalaisuusKoodi',
                        value: null},

                {translation: 'HENKILO_AIDINKIELI',
                    data: this.kieliKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                    inputValue: 'aidinkieli.kieliKoodi',
                    value: this.henkiloUpdate.aidinkieli && this.kieliKoodis.filter(kieli =>
                    kieli.value === this.henkiloUpdate.aidinkieli.kieliKoodi)[0][this.props.locale],
                    selectValue: this.henkiloUpdate.aidinkieli && this.henkiloUpdate.aidinkieli.kieliKoodi},
                {translation: 'HENKILO_HETU', value: this.henkiloUpdate.hetu, inputValue: 'hetu'},
                {translation: 'HENKILO_KAYTTAJANIMI', value: kayttajatieto.username, inputValue: 'kayttajanimi'},
                {translation: 'HENKILO_ASIOINTIKIELI',
                    data: this.kieliKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                    inputValue: 'asiointiKieli.kieliKoodi',
                    value: this.henkiloUpdate.asiointiKieli && this.kieliKoodis.filter(kieli =>
                    kieli.value === this.henkiloUpdate.asiointiKieli.kieliKoodi)[0][this.props.locale],
                    selectValue: this.henkiloUpdate.asiointiKieli && this.henkiloUpdate.asiointiKieli.kieliKoodi},
                {translation: 'HENKILO_PASSWORD', value: null, showOnlyOnWrite: true},
                {translation: 'HENKILO_PASSWORDAGAIN', value: null, showOnlyOnWrite: true},
            ],
            contactInfo: this.henkiloUpdate.yhteystiedotRyhma.map((yhteystiedotRyhma, idx) =>
                yhteystiedotRyhma.yhteystieto.map((yhteystieto, idx2) =>
                    ({translation: yhteystieto.yhteystietoTyyppi, value: yhteystieto.yhteystietoArvo,
                        inputValue: 'yhteystiedotRyhma.' + idx + '.yhteystieto.' + idx2 + '.yhteystietoArvo'})
                )
            ).reduce((a,b) => a.concat(b)),
            organisationInfo: organisations.map(organisation =>
                ({name: organisation.nimi[this.props.locale], typesFlat: organisation.tyypit && organisation.tyypit.reduce((type1, type2) => type1.concat(', ', type2)),
                    role: organisation.orgHenkilo.tehtavanimike, passive: organisation.orgHenkilo.passivoitu})),
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
                            !values.showOnlyOnWrite || !this.state.readOnly
                                ? <div key={idx} id={values.translation}>
                                    <Columns columns={2}>
                                        <span className="strong">{L[values.translation]}</span>
                                        <Field inputValue={values.inputValue} changeAction={this._updateModelField}
                                               readOnly={this.state.readOnly} data={values.data}
                                               selectValue={values.selectValue}>
                                            {values.value}
                                        </Field>
                                    </Columns>
                                </div>
                                : null
                            )}
                        </div>
                    </div>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_YHTEYSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent">
                            {this.state.contactInfo.map((values, idx) =>
                                <div key={idx} id={values.translation}>
                                    <Columns columns={2}>
                                        <span className="strong">{L[values.translation]}</span>
                                        <Field inputValue={values.inputValue} changeAction={this._updateModelField}
                                               readOnly={this.state.readOnly}>{values.value}</Field>
                                    </Columns>
                                </div>
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
                                        <div><span className="strong">{values.name} ({values.typesFlat})</span></div>
                                        <div>
                                            <span className="strong">{L['HENKILO_TEHTAVANIMIKE']}:</span>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                            <span>{values.role}</span>
                                        </div>
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
                        <Button big action={this._edit}>{L['MUOKKAA_LINKKI']}</Button>
                        <Button big action={() => {}}>{L['YKSILOI_LINKKI']}</Button>
                        <Button big action={() => {}}>{L['PASSIVOI_LINKKI']}</Button>
                        <Button big action={() => {}}>{L['LISAA_HAKA_LINKKI']}</Button>
                    </div>
                    : <div className="henkiloViewEditButtons">
                        <Button big action={this._discard}>{L['PERUUTA_LINKKI']}</Button>
                        <Button confirm big action={this._update}>{L['TALLENNA_LINKKI']}</Button>
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
    },
    _update: function () {
        updateHenkilo(this.henkiloUpdate);
    },
    _updateModelField: function (event) {
        const value = event.target.value;
        const fieldpath = event.target.name;
        this._updateFieldByDotAnnotation(this.henkiloUpdate, fieldpath, value);
    },
    _updateFieldByDotAnnotation: function(obj, path, value) {
        let schema = obj;  // a moving reference to internal objects within obj
        const pList = path.split('.');
        const len = pList.length;
        for(let i = 0; i < len-1; i++) {
            let elem = pList[i];
            if( !schema[elem] ) {
                schema[elem] = {};
            }
            schema = schema[elem];
        }

        schema[pList[len-1]] = value;
    }
});

export default HenkiloViewUserContent
