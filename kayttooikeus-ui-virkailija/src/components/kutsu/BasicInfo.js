import React from 'react'
import Select2 from 'react-select2-wrapper';

import basicInfo from '../../logic/basicInfo'

const BasicInfo = React.createClass({
  componentDidMount: function() {
    basicInfo.setLanguage(this.props.languages[0].code)
  },

  render: function() {
    const L = this.props.l10n;
    const langs = this.props.languages;
    const basicInfo = this.props.basicInfo;
    return (
      <fieldset className="basic-info">
        <h2>{L['VIRKAILIJAN_TIEDOT_OTSIKKO']}</h2>
        <div className="row">
          <label htmlFor="email">{L['VIRKAILIJAN_TIEDOT_SPOSTI']}</label>
          <input type="text" id="email" value={basicInfo.email || ''} onChange={this.handleEmail}/>
        </div>
        <div className="row">
          <label htmlFor="lang">{L['VIRKAILIJAN_TIEDOT_KIELI']}</label>
          <Select2 id="lang" data={langs.map(lang => ({id: lang.code, text:lang.name[this.props.locale]}))}
                   onSelect={this.selectLanguage} value={basicInfo.languageCode}>
            {langs.map(this.renderLang)}
          </Select2>
        </div>
      </fieldset>
    )
  },

  renderLang: function(lang) {
    return (
      <option key={lang.code} value={lang.code}>
        {lang.name[this.props.locale]}
      </option>
    )
  },

  handleEmail: function(e) {
    basicInfo.setEmail(e.target.value)
  },

  selectLanguage: function(e) {
    basicInfo.setLanguage(e.target.value)
  }
});

export default BasicInfo
