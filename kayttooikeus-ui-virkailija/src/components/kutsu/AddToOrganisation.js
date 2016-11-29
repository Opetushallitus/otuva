import React from 'react'
import PureRenderMixin from 'react-addons-pure-render-mixin';
import R from 'ramda'

import organisations, {addSelectedOrganization, changeOrganization} from '../../logic/organisations'
import AddedOrganisations from './AddedOrganisations'
import OrgSelect2 from './OrgSelect2'
import { toLocalizedText } from '../../logic/localizabletext'


const AddToOrganisation = React.createClass({
  mixins: [PureRenderMixin],
  
  render: function() {
    const L = this.props.l10n;
    const orgs = this.props.orgs;
    const uiLang = this.props.uiLang;
    const organisaatioNimi = org => toLocalizedText(uiLang, org.organisaatio.nimi);
    const mapOrgOption = org => ({
      id: org.organisaatio.oid,
      text: `${organisaatioNimi(org)} (${org.organisaatio.tyypit.join(',')})`,
      visibleText: `${organisaatioNimi(org)} (${org.organisaatio.tyypit.join(',')})`,
      level: org.organisaatio.level
    });
    return (
      <fieldset className="add-to-organisation">
        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <AddedOrganisations changeOrganization={e => oldId => this.changeOrganization(oldId, e)} orgs={this.props.orgs}
            addedOrgs={this.props.addedOrgs} l10n={this.props.l10n} uiLang={this.props.uiLang} />
        <div className="row">
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <OrgSelect2 id="org" onSelect={this.selectOrganisation} data={orgs.map(mapOrgOption)}/>
        </div>
      </fieldset>
    )
  },
  
  changeOrganization: function(oldId, e) {
    const selectedOrganization = R.find(R.pathEq(['organisaatio', 'oid'], e.target.value))(this.props.orgs);
    if (selectedOrganization) {
      changeOrganization(oldId, selectedOrganization.organisaatio, this.props.omaOid);
    }
  },

  selectOrganisation: function(e) {
    const selectedOrganization = R.find(R.pathEq(['organisaatio', 'oid'], e.target.value))(this.props.orgs);
    if (selectedOrganization) {
      addSelectedOrganization(selectedOrganization.organisaatio, this.props.omaOid);
    }
  }
});

export default AddToOrganisation
