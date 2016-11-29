import React from 'react'
import R from 'ramda'
import Button from 'button'

import organisations from '../../logic/organisations'
import { toLocalizedText } from '../../logic/localizabletext'
import Select2 from 'select';
import OrgSelect2 from './OrgSelect2'

import './AddedOrganisation.css'

const AddedOrganisation = React.createClass({
  render: function() {
    const addedOrg = this.props.addedOrg;
    const selectablePermissions = R.difference(addedOrg.selectablePermissions, addedOrg.selectedPermissions);
    const L = this.props.l10n;
    const uiLang = this.props.uiLang;
    const orgs = this.props.orgs;
    const organisaatioNimi = org => toLocalizedText(uiLang, org.organisaatio.nimi);
    const mapOrgOption = org => ({
      id: org.organisaatio.oid,
      text: `${organisaatioNimi(org)} (${org.organisaatio.tyypit.join(',')})`,
      visibleText: `${organisaatioNimi(org)} (${org.organisaatio.tyypit.join(',')})`,
      level: org.organisaatio.level
    });
    return (
      <div className="added-org" key={addedOrg.organisation.oid}>
        <h3>{toLocalizedText(this.props.uiLang, addedOrg.organisation.nimi)}
          <Button className="cancel right" action={this.removeAddedOrg.bind(null, addedOrg.organisation.oid)}>X</Button>
        </h3>

        <div className="row">
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <OrgSelect2 id="org" onSelect={this.props.changeOrganization(addedOrg.organisation.id)} data={orgs.map(mapOrgOption)}
              value={addedOrg.organisation.oid}/>
        </div>
        
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
    e.preventDefault();
    organisations.removeById(id);
  },

  selectPermissions: function(e) {
    const selectedIds = Array.apply(null, e.target.options)
      .filter(option => option.selected)
      .map(option => option.value)
      .map(value => parseInt(value, 10));
    const selectedPermissions = R.filter((permission) => selectedIds.includes(permission.ryhmaId), this.props.addedOrg.selectablePermissions);
    this.props.addedOrg.selectedPermissions = R.union(this.props.addedOrg.selectedPermissions, selectedPermissions);
    this.forceUpdate()
  },

  removeAddedPermission: function(id, e) {
    e.preventDefault();
    this.props.addedOrg.selectedPermissions = R.reject(permission => permission.ryhmaId === id, this.props.addedOrg.selectedPermissions);
    this.forceUpdate()
  }

});

export default AddedOrganisation
