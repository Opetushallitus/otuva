import React from 'react'

import basicInfo from '../logic/basicInfo'

const BasicInfo = React.createClass({

  componentDidMount: function() {
    basicInfo.setLanguage(this.props.languages[0].code)
  },

  render: function() {
    const L = this.props.l10n 
    const langs = this.props.languages

    return (
      <fieldset className="basic-info">
        <h2>{L['VIRKAILIJAN_TIEDOT_OTSIKKO']}</h2>
        <div>
          <label htmlFor="email">{L['VIRKAILIJAN_TIEDOT_SPOSTI']}</label>
          <input type="text" id="email" onChange={this.handleEmail}/>
        </div>
        <div>
          <label htmlFor="lang">{L['VIRKAILIJAN_TIEDOT_KIELI']}</label>
          <select id="lang" onChange={this.selectLanguage}>
            {langs.map(this.renderLang)}
          </select>
        </div>
      </fieldset>
    )
  },

  renderLang: function(lang) {
    return (
      <option key={lang.code} value={lang.code}>
        {lang['name-' + this.props.uiLang]}
      </option>  
    )
  },

  handleEmail: function(e) {
    basicInfo.setEmail(e.target.value)
  },

  selectLanguage: function(e) {
    basicInfo.setLanguage(e.target.value)
  },

})

export default BasicInfo
