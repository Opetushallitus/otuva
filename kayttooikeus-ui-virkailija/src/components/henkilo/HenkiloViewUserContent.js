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
        this.kansalaisuusKoodis = this.props.koodistoKansalaisuus.result.map(koodi => (
            {value: koodi.koodiArvo.toLowerCase(),
                ...koodi.metadata.map(kansalaisuusKoodi =>
                    ({[kansalaisuusKoodi.kieli.toLowerCase()]: kansalaisuusKoodi.nimi})).reduce((a,b) => {
                    a[Object.keys(b)[0]] = b[Object.keys(b)[0]];
                    return a
                }, {})
            }
        ));
        this.sukupuoliKoodis = this.props.koodistoSukupuoli.result.map(koodi => (
            {value: koodi.koodiArvo.toLowerCase(),
                ...koodi.metadata.map(sukupuoliKoodi => (
                    {[sukupuoliKoodi.kieli.toLowerCase()]: sukupuoliKoodi.nimi}
                )).reduce((a,b) => {
                    a[Object.keys(b)[0]] = b[Object.keys(b)[0]];
                    return a
                }, {})}
        ));

        return {
            readOnly: this.props.readOnly,
            showPassive: false,
            basicInfo: [
                {translation: 'HENKILO_ETUNIMET', value: this.henkiloUpdate.etunimet, inputValue: 'etunimet'},
                {translation: 'HENKILO_SUKUNIMI', value: this.henkiloUpdate.sukunimi, inputValue: 'sukunimi'},
                {translation: 'HENKILO_SYNTYMAAIKA', value: this.henkiloUpdate.syntymaaika, inputValue: 'syntymaaika'},
                {translation: 'HENKILO_HETU', value: this.henkiloUpdate.hetu, inputValue: 'hetu'},
                {translation: 'HENKILO_KUTSUMANIMI', value: this.henkiloUpdate.kutsumanimi, inputValue: 'kutsumanimi'},
            ],
            basicInfo2: [
                this.henkiloUpdate.kansalaisuus && this.henkiloUpdate.kansalaisuus.length
                    ? this.henkiloUpdate.kansalaisuus.map((values, idx) => ({translation: 'HENKILO_KANSALAISUUS',
                        data: this.kansalaisuusKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                        value: this.kansalaisuusKoodis.filter(kansalaisuus =>
                        kansalaisuus.value === values.kansalaisuusKoodi)[0][this.props.locale],
                        inputValue: 'kansalaisuus.' + idx + '.kansalaisuusKoodi',
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
                {translation: 'HENKILO_SUKUPUOLI',
                    data: this.sukupuoliKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                    inputValue: 'sukupuoli',
                    value: this.henkiloUpdate.sukupuoli && this.sukupuoliKoodis.filter(sukupuoli =>
                    sukupuoli.value === this.henkiloUpdate.sukupuoli)[0][this.props.locale],
                    selectValue: this.henkiloUpdate.sukupuoli},
                {translation: 'HENKILO_ASIOINTIKIELI',
                    data: this.kieliKoodis.map(koodi => ({id: koodi.value, text: koodi[this.props.locale]})),
                    inputValue: 'asiointiKieli.kieliKoodi',
                    value: this.henkiloUpdate.asiointiKieli && this.kieliKoodis.filter(kieli =>
                    kieli.value === this.henkiloUpdate.asiointiKieli.kieliKoodi)[0][this.props.locale],
                    selectValue: this.henkiloUpdate.asiointiKieli && this.henkiloUpdate.asiointiKieli.kieliKoodi},
            ],
            loginInfo: [
                {translation: 'HENKILO_KAYTTAJANIMI', value: kayttajatieto.username, inputValue: 'kayttajanimi'},
                {translation: 'HENKILO_PASSWORD', value: null, showOnlyOnWrite: false},
                {translation: 'HENKILO_PASSWORDAGAIN', value: null, showOnlyOnWrite: true},
            ]
        }
    },
    render: function() {
        const L = this.props.l10n;
        return (
            <div className="henkiloViewUserContentWrapper">
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_PERUSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <Columns columns={3}>
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
                            <div className="henkiloViewContent">
                                {this.state.basicInfo2.map((values, idx) =>
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
                            <div className="henkiloViewContent">
                                {this.state.loginInfo.map((values, idx) =>
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
                        </Columns>
                    </div>
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
