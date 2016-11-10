import React from 'react'
import R from 'ramda'

import organisations from '../logic/organisations'
import { toLocalizedText } from '../logic/localizabletext'

import './AddedOrganisation.css'

const AddedOrganisation = React.createClass({

  render: function() {
    const addedOrg = this.props.addedOrg
    const selectablePermissions = R.difference(addedOrg.selectablePermissions, addedOrg.selectedPermissions)
    const L = this.props.l10n
    return (
      <div className="added-org" key={addedOrg.organisation.oid}>
        <h3>{toLocalizedText(this.props.uiLang, addedOrg.organisation.nimi, addedOrg.organisation.oid)}
          <a href=""
            onClick={this.removeAddedOrg.bind(null, addedOrg.organisation.oid)}>X</a>
        </h3>
        <div>
          <label htmlFor="permissions">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA']}
          </label>
          <select onChange={this.selectPermissions} multiple
            id="permissions">
            {selectablePermissions.map(this.renderPermission)}
          </select>
        </div>
        <ul>
          {addedOrg.selectedPermissions.map(permission => {
            return (
              <li key={permission.id}>
                {toLocalizedText(this.props.uiLang, permission.description, permission.name)}
                <a href="" onClick={this.removeAddedPermission.bind(null, permission.id)}>X</a>
              </li>
            )
          })}
        </ul>
      </div>
    )
  },

  renderPermission: function(permission) {
    return (
      <option key={permission.id} value={permission.id}>
        {toLocalizedText(this.props.uiLang, permission.description, permission.name)}
      </option>
    )
  },

  removeAddedOrg: function(id, e) {
    e.preventDefault()
    organisations.removeById(id)
  },

  selectPermissions: function(e) {
    const selectedIds = Array.apply(null, e.target.options)
      .filter(option => option.selected)
      .map(option => option.value)
      .map(value => parseInt(value, 10))
    const selectedPermissions = R.filter((permission) => selectedIds.includes(permission.id), this.props.addedOrg.selectablePermissions)
    this.props.addedOrg.selectedPermissions = R.union(this.props.addedOrg.selectedPermissions, selectedPermissions)
    this.forceUpdate()
  },

  removeAddedPermission: function(id, e) {
    e.preventDefault()
    this.props.addedOrg.selectedPermissions = R.reject(permission => permission.id === id, this.props.addedOrg.selectedPermissions)
    this.forceUpdate()
  }

})

export default AddedOrganisation
