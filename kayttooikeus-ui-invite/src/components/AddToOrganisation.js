import React from 'react'
import R from 'ramda'

import organisations from '../logic/organisations'
import AddedOrganisations from './AddedOrganisations'
import { toLocalizedText } from '../logic/localizabletext'

const AddToOrganisation = React.createClass({

  render: function() {
    const L = this.props.l10n
    const orgs = this.props.orgs.organisaatiot

    return (
      <fieldset className="add-to-organisation">

        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <AddedOrganisations addedOrgs={this.props.addedOrgs} l10n={this.props.l10n} uiLang={this.props.uiLang} />

        <div>
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <select id="org" onChange={this.selectOrganisation}>
            <option value=""></option>
            {orgs.map(this.renderOrganisation)}
          </select>
        </div>

      </fieldset>
    )
  },

  selectOrganisation: function(e) {
    const selectedOrganization = R.find(R.propEq('oid', e.target.value))(this.props.orgs.organisaatiot)
    if (selectedOrganization) {
      const oid = selectedOrganization.oid
      // TODO: use kayttooikeus-service
      fetch(`/authentication-service/resources/kayttooikeusryhma/organisaatio/${oid}`, {
        credentials: 'same-origin'
      }).then(response => {
        return response.json()
      }).then(permissions => {
        organisations.add({
          id: selectedOrganization.oid,
          organisation: selectedOrganization,
          selectablePermissions: permissions,
          selectedPermissions: [],
        })
      })
    }
  },

  renderOrganisation: function(org) {
    const organisaatiotyypit = org.organisaatiotyypit.join(',')
    return (
      <option key={org.oid} value={org.oid}>
        {toLocalizedText(this.props.uiLang, org.nimi, org.oid)} ({organisaatiotyypit})
      </option>
    )
  },

})

export default AddToOrganisation
