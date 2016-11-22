import React from 'react'
import PureRenderMixin from 'react-addons-pure-render-mixin';
import R from 'ramda'

import organisations from '../../logic/organisations'
import AddedOrganisations from './AddedOrganisations'
import { toLocalizedText } from '../../logic/localizabletext'

const AddToOrganisation = React.createClass({
  mixins: [PureRenderMixin],

  organisaatioNimi: function(organisaatio) {
    return toLocalizedText(this.props.uiLang, organisaatio.nimi)
  },

  render: function() {
    const L = this.props.l10n;
    const orgs = this.props.orgs;

    return (
      <fieldset className="add-to-organisation">

        <h3>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h3>

        <AddedOrganisations addedOrgs={this.props.addedOrgs} l10n={this.props.l10n} uiLang={this.props.uiLang} />

        <div>
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <select id="org" onChange={this.selectOrganisation}>
            <option value=""></option>
            {R.sortBy(this.organisaatioNimi)(orgs).map(this.renderOrganisation)}
          </select>
        </div>

      </fieldset>
    )
  },

  selectOrganisation: function(e) {
    const selectedOrganization = R.find(R.pathEq(['organisaatio', 'oid'], e.target.value))(this.props.orgs)
    if (selectedOrganization) {
      const henkiloOid = this.props.omaOid;
      const organisaatioOid = selectedOrganization.organisaatio.oid;
      fetch(`/kayttooikeus-service/kayttooikeusryhma/${henkiloOid}/${organisaatioOid}`, {
        credentials: 'same-origin'
      }).then(response => {
        return response.json()
      }).then(permissions => {
        organisations.add({
          id: selectedOrganization.organisaatio.oid,
          organisation: selectedOrganization.organisaatio,
          selectablePermissions: permissions,
          selectedPermissions: []
        })
      })
    }
  },

  renderOrganisation: function(org) {
    const organisaatiotyypit = org.organisaatio.tyypit.join(',');
    return (
      <option key={org.organisaatio.oid} value={org.organisaatio.oid}>
        {this.organisaatioNimi(org.organisaatio)} ({organisaatiotyypit})
      </option>
    )
  }

});

export default AddToOrganisation
