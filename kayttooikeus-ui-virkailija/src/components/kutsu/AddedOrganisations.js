import React from 'react'

import AddedOrganisation from './AddedOrganisation'

const AddedOrganisations = React.createClass({
  render: function() {
    return (
      <div>
        {this.props.addedOrgs.map(this.renderAddedOrganisation)}
      </div>
    )
  },

  renderAddedOrganisation: function(addedOrg) {
    return (
      <AddedOrganisation key={addedOrg.id} orgs={this.props.orgs}
                         addedOrg={addedOrg}
                         changeOrganization={this.props.changeOrganization}
                         l10n={this.props.l10n}
                         uiLang={this.props.uiLang} />
    )
  }
});

export default AddedOrganisations
