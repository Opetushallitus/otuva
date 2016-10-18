import React from 'react'

import basicInfo from '../../basicInfo'

const VirkailijanTiedot = React.createClass({

  componentDidMount: function() {
    basicInfo.setLanguage(this.props.kielet[0].code)
  },

  render: function() {
    const L = this.props.l10n 
    return (
      <fieldset>
        <h2>Virkailijan tiedot</h2>
        <div>
          <label htmlFor="email">{L['VIRKAILIJAN_TIEDOT_SPOSTI']}</label>
          <input type="text" id="email" onChange={this.handleEmail}/>
        </div>
        <div>
          <label htmlFor="lang">{L['VIRKAILIJAN_TIEDOT_KIELI']}</label>
          <select id="lang" onChange={this.selectLanguage}>
            {this.props.kielet.map(this.renderKieli)}
          </select>
        </div>
      </fieldset>
    )
  },

  renderKieli: function(kieli) {
    return (
      <option 
        key={kieli.code} 
        value={kieli.code}>
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

export default VirkailijanTiedot
