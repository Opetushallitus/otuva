import React from 'react'
import R from 'ramda'

import organisations from '../logic/organisations'
import AddedOrganisations from './AddedOrganisations'

const AddToOrganisation = React.createClass({

  getInitialState: function() {
    return {
      selectedOrganisation: '',
      selectedPermissions: [],
    }
  },

  componentWillMount: function() {
    this.setState({
      selectedOrganisation: this.props.orgs[0].id
    })
  },

  render: function() {
    const L = this.props.l10n
    const orgs = this.props.orgs
    const selectedOrganisation = this.state.selectedOrganisation

    const activeOrg = 
      R.find(R.propEq('id', selectedOrganisation))(orgs)
    const permissions = activeOrg.permissions

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

        <AddedOrganisations addedOrgs={this.props.addedOrgs} />

      </fieldset>
    )
  },

  addOrganisation: function(e) {
    e.preventDefault()

    if (this.state.selectedPermissions.length > 0) {
      organisations.add({
        id: this.state.selectedOrganisation,
        permissions: this.state.selectedPermissions
      })
    }
  },

  selectOrganisation: function(e) {
    this.setState({
      selectedOrganisation: e.target.value,
      selectedPermissions: []
    }) 
  },

  selectPermissions: function(e) {
    this.setState({
      selectedPermissions: Array.apply(null, e.target.options)
        .filter(option => option.selected)
        .map(option => option.value)
    })
  },

  renderOrganisation: function(org) {
    return (
      <option key={org.id} value={org.id}>
        {org['name-' + this.props.uiLang]}
      </option>  
    )
  },

  renderPermission: function(permission) {
    return (
      <option key={permission.id} value={permission.id}>
        {permission['name-' + this.props.uiLang]}
      </option>  
    )
  },

})

export default AddToOrganisation
