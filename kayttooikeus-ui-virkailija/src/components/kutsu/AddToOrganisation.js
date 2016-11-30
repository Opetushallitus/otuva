import React from 'react'
import PureRenderMixin from 'react-addons-pure-render-mixin';
import R from 'ramda'

import {addEmptyOrganization, changeOrganization} from '../../logic/organisations'
import AddedOrganisations from './AddedOrganisations'

const AddToOrganisation = React.createClass({
  mixins: [PureRenderMixin],
  
  render: function() {
    const L = this.props.l10n;
    return (
      <fieldset className="add-to-organisation">
        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <AddedOrganisations changeOrganization={oldId => e => this.changeOrganization(oldId, e)} orgs={this.props.orgs}
            addedOrgs={this.props.addedOrgs} l10n={this.props.l10n} uiLang={this.props.uiLang} />
        <div className="row">
          <a href="#" onClick={this.addEmptyOrganization}>{L['VIRKAILIJAN_KUTSU_LISAA_ORGANISAATIO_LINKKI']}</a>
        </div>
      </fieldset>
    )
  },

  addEmptyOrganization: function(e) {
    e.preventDefault();
    addEmptyOrganization();
  },

  changeOrganization: function(oldOid, e) {
    const selectedOrganization = R.find(R.pathEq(['oid'], e.target.value))(this.props.orgs);
    console.info('changeOrganization', e.target.value, selectedOrganization);
    if (selectedOrganization) {
      changeOrganization(oldOid, selectedOrganization, this.props.omaOid);
    }
  }
});

export default AddToOrganisation
