import React from 'react'

import organisations from '../logic/organisations'

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
      <div className="added-org" key={addedOrg.id}>
        <h3>{addedOrg.id} 
          <a href="" 
            onClick={this.removeAddedOrg.bind(null, addedOrg.id)}>X</a>
        </h3>
        <ul> 
          {addedOrg.permissions.map(permission => {
            return <li key={permission}>{permission}</li>
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
