import React from 'react'
import R from 'ramda'
import Button from 'button'

import organisations from '../../logic/organisations'
import { toLocalizedText } from '../../logic/localizabletext'
import Select2 from 'react-select2-wrapper';

import './AddedOrganisation.css'


const AddedOrganisation = React.createClass({
  render: function() {
    const addedOrg = this.props.addedOrg;
    const selectablePermissions = R.difference(addedOrg.selectablePermissions, addedOrg.selectedPermissions);
    const L = this.props.l10n;
    const uiLang = this.props.uiLang;
    return (
      <div className="added-org" key={addedOrg.organisation.oid}>
        <h3>{toLocalizedText(this.props.uiLang, addedOrg.organisation.nimi)}
          <Button className="cancel right" action={this.removeAddedOrg.bind(null, addedOrg.organisation.oid)}>X</Button>
        </h3>
        <div className="row">
          <label htmlFor="permissions">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA']}
          </label>
          <Select2 onSelect={this.selectPermissions} multiple id="permissions"
                 data={selectablePermissions.map(permission => ({id: permission.ryhmaId, text: toLocalizedText(uiLang, permission.ryhmaNames)}))}>
          </Select2>
        </div>
        <ul>
          {addedOrg.selectedPermissions.map(permission => {
            return (
              <li key={permission.ryhmaId}>
                {toLocalizedText(this.props.uiLang, permission.ryhmaNames)}
                <Button className="cancel right" action={this.removeAddedPermission.bind(null, permission.ryhmaId)}>X</Button>
              </li>
            )
          })}
        </ul>
      </div>
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
    const selectedPermissions = R.filter((permission) => selectedIds.includes(permission.ryhmaId), this.props.addedOrg.selectablePermissions)
    this.props.addedOrg.selectedPermissions = R.union(this.props.addedOrg.selectedPermissions, selectedPermissions)
    this.forceUpdate()
  },

  removeAddedPermission: function(id, e) {
    e.preventDefault()
    this.props.addedOrg.selectedPermissions = R.reject(permission => permission.ryhmaId === id, this.props.addedOrg.selectedPermissions)
    this.forceUpdate()
  }

})

export default AddedOrganisation
