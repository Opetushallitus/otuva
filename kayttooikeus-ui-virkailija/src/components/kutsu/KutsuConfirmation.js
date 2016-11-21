import React from 'react'
import R from 'ramda'
import Modal from 'modal'

import {kutsu} from '../../external/kutsu'
import {toLocalizedText} from '../../logic/localizabletext'

const KutsuConfirmation = React.createClass({

  getInitialState: function() {
    return {
      sent: false
    }
  },

  render: function() {
    const L = this.props.l10n;

    return (
      <Modal show={this.props.modalOpen} onClose={this.props.modalCloseFn} closeOnOuterClick={true}>
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
    const orgName = toLocalizedText(this.props.locale, org.organisation.nimi);
    return (
      <div key={org.organisation.oid}>
        <h3>{orgName}</h3>
        {org.selectedPermissions.map(this.renderAddedOrgPermission)}
      </div>
    )
  },

  renderAddedOrgPermission: function(permission) {
    return (
      <div key={permission.ryhmaId}>
        <h4>{toLocalizedText(this.props.locale, permission.ryhmaNames)}</h4>
      </div>
    )
  },

  sendInvitation: function(e) {
    e.preventDefault();

    const payload = {
      sahkoposti: this.props.basicInfo.email,
      asiointikieli: this.props.basicInfo.languageCode,
      organisaatiot: R.map(addedOrg => ({
          organisaatioOid: addedOrg.id,
          kayttoOikeusRyhmat: R.map(selectedPermission => ({
              id: selectedPermission.ryhmaId
            }))(addedOrg.selectedPermissions)
        }))(this.props.addedOrgs)
    };
    const {invitationResponseS} = kutsu(payload);

    invitationResponseS.onValue(response => {
      console.info('inviation sent', response);
      this.setState({sent: true});
    });
    invitationResponseS.onError(error => {
      console.error('invitation send error: ', error);
    });
  }
});

export default KutsuConfirmation
