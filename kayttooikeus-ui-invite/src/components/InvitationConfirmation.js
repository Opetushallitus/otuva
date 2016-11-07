import React from 'react'
import Modal from 'modal'

import invite from '../external/invitation'
import { toLocalizedText } from '../logic/localizabletext'

const InvitationConfirmation = React.createClass({

  getInitialState: function() {
    return {
      sent: false,
    }
  },

  render: function() {
    const L = this.props.l10n

    return (
      <Modal show={this.props.modalOpen}
        onClose={this.props.modalCloseFn} closeOnOuterClick={true}>

        <div className="confirmation-modal">
          <h1>{L['VIRKAILIJAN_LISAYS_ESIKATSELU_OTSIKKO']}</h1>
          <p>{L['VIRKAILIJAN_LISAYS_ESIKATSELU_TEKSTI']} {this.props.basicInfo.email}</p>
          <h2>{L['VIRKAILIJAN_LISAYS_ESIKATSELU_ALAOTSIKKO']}</h2>
          {this.props.addedOrgs.map(this.renderAddedOrg)}
          <button onClick={this.sendInvitation}>
            {L['VIRKAILIJAN_LISAYS_TALLENNA']}
          </button>
          <button onClick={this.props.modalCloseFn}>
            {L['VIRKAILIJAN_LISAYS_ESIKATSELU_SULJE']}
          </button>
          <p>{this.state.sent ? L['VIRKAILIJAN_LISAYS_LAHETETTY'] : ''}</p>
        </div>

      </Modal>
    )
  },

  renderAddedOrg: function(org) {
    const orgName = toLocalizedText(this.props.uiLang, org.organisation.nimi, org.organisation.oid)

    return (
      <div key={org.organisation.oid}>
        <h3>{orgName}</h3>
        {org.permissions.map(this.renderAddedOrgPermission)}
      </div>
    )
  },

  renderAddedOrgPermission: function(permission) {
    return (
      <div key={permission.id}>
        <h4>{toLocalizedText(this.props.uiLang, permission.description, permission.name)}</h4>
      </div>
    )
  },

  sendInvitation: function(e) {
    e.preventDefault()

    const payload = {
      orgs: this.props.addedOrgs,
      info: this.props.basicInfo
    }
    const { invitationResponseS } = invite(payload)

    invitationResponseS.onValue(response => {
      console.log(response)
      this.setState({
        sent: true
      })
    })
  },

})

export default InvitationConfirmation
