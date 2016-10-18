import React from 'react'

import VirkailijanTiedot from '../VirkailijanTiedot/VirkailijanTiedot'
import VirkailijanLisaysOrganisaatioon from 
  '../VirkailijanLisaysOrganisaatioon/VirkailijanLisaysOrganisaatioon'

import './VirkailijanLisaysLomake.css'

const VirkailijanLisaysLomake = React.createClass({

  render: function() {
    const uiLang = 'fi'
    const l10n = this.props.l10n[uiLang]
    return (
      <form>
        <h1>{l10n['VIRKAILIJAN_LISAYS_OTSIKKO']}</h1>
        <VirkailijanTiedot 
          kielet={this.props.languages}
          l10n={l10n}
          uiLang={uiLang} />
        <VirkailijanLisaysOrganisaatioon
          organisaatiot={this.props.organisaatiot}
          addedOrgs={this.props.addedOrgs}
          l10n={l10n}
          uiLang={uiLang} />
        <button onClick={this.handleSubmit}>
          {l10n['VIRKAILIJAN_LISAYS_TALLENNA']}
        </button>
      </form>
    );
  },

  handleSubmit: function(e) {
    e.preventDefault()
  },
  
})

export default VirkailijanLisaysLomake