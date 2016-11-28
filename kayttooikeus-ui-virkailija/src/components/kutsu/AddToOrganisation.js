import React from 'react'
import PureRenderMixin from 'react-addons-pure-render-mixin';
import R from 'ramda'
import Select2 from 'react-select2-wrapper';

import organisations from '../../logic/organisations'
import AddedOrganisations from './AddedOrganisations'
import { toLocalizedText } from '../../logic/localizabletext'

const AddToOrganisation = React.createClass({
  mixins: [PureRenderMixin],
  
  render: function() {
    const L = this.props.l10n;
    const orgs = this.props.orgs;
    const uiLang = this.props.uiLang;
    const organisaatioNimi = org => toLocalizedText(uiLang, org.organisaatio.nimi);

    return (
      <fieldset className="add-to-organisation">
        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <AddedOrganisations addedOrgs={this.props.addedOrgs} l10n={this.props.l10n} uiLang={this.props.uiLang} />

        <div className="row">
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <Select2 id="org" onSelect={this.selectOrganisation} data={R.sortBy(organisaatioNimi)(orgs).map(org => ({id: org.organisaatio.oid, text: `${organisaatioNimi(org)} (${org.organisaatio.tyypit.join(',')})`}))}/>
        </div>
      </fieldset>
    )
  },

  selectOrganisation: function(e) {
    const selectedOrganization = R.find(R.pathEq(['organisaatio', 'oid'], e.target.value))(this.props.orgs)
    if (selectedOrganization) {
      const henkiloOid = this.props.omaOid;
      const organisaatioOid = selectedOrganization.organisaatio.oid;
      fetch(window.url('kayttooikeus-service.kayttooikeusryhma.forHenkilo.inOrganisaatio', henkiloOid, organisaatioOid), {
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
  }

});

export default AddToOrganisation
