import React from 'react'

import BasicInfo from './BasicInfo'
import AddToOrganisation from './AddToOrganisation'
import InvitationConfirmation from './InvitationConfirmation'

import './InvitationForm.css'

const InvitationForm = React.createClass({

  getInitialState: function() {
    return {
      confirmationModalOpen: false
    }
  },

  render: function() {
    const uiLang = 'fi'
    const L = this.props.l10n[uiLang]
    const props = {...this.props, uiLang, l10n: L}
    const confirmationProps = {
      ...props,
      modalCloseFn: this.closeConfirmationModal,
      modalOpen: this.state.confirmationModalOpen
    }

    return (
      <form className="wrapper">

        <div className="header">
          <h1>{L['VIRKAILIJAN_LISAYS_OTSIKKO']}</h1>
        </div>

        <BasicInfo {...props} />
        <AddToOrganisation {...props} />

        <div className="footer">
          <button onClick={this.openConfirmationModal}>
            {L['VIRKAILIJAN_LISAYS_TALLENNA']}
          </button>
        </div>

        <InvitationConfirmation {...confirmationProps} />

      </form>
    )
  },

  openConfirmationModal: function(e) {
    e.preventDefault()

    this.setState({
      confirmationModalOpen: true
    })
  },

  closeConfirmationModal: function(e) {
    e.preventDefault()

    this.setState({
      confirmationModalOpen: false
    })
  },

})

export default InvitationForm
