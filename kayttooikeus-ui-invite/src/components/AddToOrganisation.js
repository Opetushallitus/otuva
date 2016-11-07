import React from 'react'
import R from 'ramda'

import organisations from '../logic/organisations'
import permissions from '../logic/permissions'
import AddedOrganisations from './AddedOrganisations'
import { toLocalizedText } from '../logic/localizabletext'

const AddToOrganisation = React.createClass({

  getInitialState: function() {
    return {
      selectedOrganisation: '',
      selectedPermissions: [],
    }
  },

  componentWillMount: function() {
    this.setState({
      selectedOrganisation: this.props.orgs.organisaatiot[0]
    })
  },

  render: function() {
    const L = this.props.l10n
    const orgs = this.props.orgs.organisaatiot
    const permissions = this.props.permissions

    return (
      <fieldset className="add-to-organisation">

        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <div>
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <select id="org" onChange={this.selectOrganisation}>
            {orgs.map(this.renderOrganisation)}
          </select>
        </div>

        <div>
          <label htmlFor="permissions">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA']}
          </label>
          <select onChange={this.selectPermissions} multiple
            id="permissions">
            {permissions.map(this.renderPermission)}
          </select>
        </div>

        <a href="" onClick={this.addOrganisation}>
          {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_LISAA']}
        </a>

        <AddedOrganisations addedOrgs={this.props.addedOrgs} uiLang={this.props.uiLang} />

      </fieldset>
    )
  },

  addOrganisation: function(e) {
    e.preventDefault()

    if (this.state.selectedPermissions.length > 0) {
      organisations.add({
        id: this.state.selectedOrganisation.oid,
        organisation: this.state.selectedOrganisation,
        permissions: this.state.selectedPermissions
      })
    }
  },

  selectOrganisation: function(e) {
    const selectedOrganization = R.find(R.propEq('oid', e.target.value))(this.props.orgs.organisaatiot)
    this.setState({
      selectedOrganisation: selectedOrganization,
      selectedPermissions: []
    })
    permissions.populateFromOrganisaatioOid(selectedOrganization.oid)
  },

  selectPermissions: function(e) {
    const selectedIds = Array.apply(null, e.target.options)
      .filter(option => option.selected)
      .map(option => option.value)
      .map(value => parseInt(value, 10))
    const selectedPermissions = R.filter((permission) => selectedIds.includes(permission.id), this.props.permissions)
    this.setState({
      selectedPermissions: selectedPermissions
    })
  },

  renderOrganisation: function(org) {
    const organisaatiotyypit = org.organisaatiotyypit.join(',')
    return (
      <option key={org.oid} value={org.oid}>
        {toLocalizedText(this.props.uiLang, org.nimi, org.oid)} ({organisaatiotyypit})
      </option>
    )
  },

  renderPermission: function(permission) {
    return (
      <option key={permission.id} value={permission.id}>
        {toLocalizedText(this.props.uiLang, permission.description, permission.name)}
      </option>
    )
  },

})

export default AddToOrganisation
