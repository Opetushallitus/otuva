import React from 'react'

import organisations from '../logic/organisations'
import { toLocalizedText } from '../logic/localizabletext'

import './AddedOrganisations.css'

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
      <div className="added-org" key={addedOrg.organisation.oid}>
        <h3>{toLocalizedText(this.props.uiLang, addedOrg.organisation.nimi, addedOrg.organisation.oid)}
          <a href=""
            onClick={this.removeAddedOrg.bind(null, addedOrg.organisation.oid)}>X</a>
        </h3>
        <ul>
          {addedOrg.permissions.map(permission => {
            return <li key={permission.id}>{toLocalizedText(this.props.uiLang, permission.description, permission.name)}</li>
          })}
        </ul>
      </div>
    )
  },

  removeAddedOrg: function(id, e) {
    e.preventDefault()
    organisations.removeById(id)
  },

})

export default AddedOrganisations
