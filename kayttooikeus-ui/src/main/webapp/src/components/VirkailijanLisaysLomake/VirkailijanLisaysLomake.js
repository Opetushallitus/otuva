import React from 'react'

import invite from '../../external/invitation'
import VirkailijanTiedot from '../VirkailijanTiedot/VirkailijanTiedot'
import VirkailijanLisaysOrganisaatioon from 
  '../VirkailijanLisaysOrganisaatioon/VirkailijanLisaysOrganisaatioon'

import './VirkailijanLisaysLomake.css'

const VirkailijanLisaysLomake = React.createClass({

  getInitialState() {
    return {
      sent: false
    }
  },

  render: function() {
    const uiLang = 'fi'
    const L = this.props.l10n[uiLang]
    return (
      <form>
        <h1>{L['VIRKAILIJAN_LISAYS_OTSIKKO']}</h1>
        <VirkailijanTiedot 
          kielet={this.props.languages}
          l10n={L}
          uiLang={uiLang} />
        <VirkailijanLisaysOrganisaatioon
          organisaatiot={this.props.organisaatiot}
          addedOrgs={this.props.addedOrgs}
          l10n={L}
          uiLang={uiLang} />
        <button 
          onClick={this.handleSubmit}>
          {L['VIRKAILIJAN_LISAYS_TALLENNA']}
        </button>
        <p>{this.state.sent ? 'Sent' : ''}</p>
      </form>
    )
  },

  handleSubmit: function(e) {
    e.preventDefault()

    const payload = {
      orgs: this.props.addedOrgs, 
      info: this.props.basicInfo
    }
    const { invitationResponseS } = invite(payload)
    
    invitationResponseS.onValue(response => {
      this.setState({
        sent: true
      })
    })
  },
  
})

export default VirkailijanLisaysLomake
