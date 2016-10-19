import React from 'react'

import basicInfo from '../logic/basicInfo'

const BasicInfo = React.createClass({

  componentDidMount: function() {
    basicInfo.setLanguage(this.props.languages[0].code)
  },

  render: function() {
    const L = this.props.l10n 
    const kielet = this.props.languages

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
            {kielet.map(this.renderKieli)}
          </select>
        </div>
      </fieldset>
    )
  },

  renderKieli: function(kieli) {
    return (
      <option key={kieli.code} value={kieli.code}>
        {kieli['name-' + this.props.uiLang]}
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
