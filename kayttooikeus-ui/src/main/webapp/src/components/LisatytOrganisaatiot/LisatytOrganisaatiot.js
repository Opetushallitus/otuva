import React from 'react'

import organisations from '../../organisations'
import './LisatytOrganisaatiot.css'

const LisatytOrganisaatiot = React.createClass({

  render: function() {
    return (
      <div>
        {this.props.addedOrgs.map(this.renderAddedOrganisaatio)}
      </div>
    )
  },

  renderAddedOrganisaatio: function(addedOrg) {
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

export default LisatytOrganisaatiot
