import React from 'react'
import R from 'ramda'

import organisations from '../logic/organisations'
import LisatytOrganisaatiot from './LisatytOrganisaatiot'

const VirjailijanLisaysOrganisaatioon = React.createClass({

  getInitialState: function() {
    return {
      selectedOrganisaatio: '',
      selectedKayttooikeuses: [],
    }
  },

  componentWillMount: function() {
    this.setState({
      selectedOrganisaatio: this.props.organisaatiot[0].id
    })
  },

  render: function() {
    const L = this.props.l10n
    const organisaatiot = this.props.organisaatiot
    const selectedOrganisaatio = this.state.selectedOrganisaatio

    const activeOrg = 
      R.find(R.propEq('id', selectedOrganisaatio))(organisaatiot)
    const kayttooikeudet = activeOrg.permissions

    return (
      <fieldset className="virkailijan-lisays-organisaatioon">
      
        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>

        <div>
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <select id="org" onChange={this.selectOrganisaatio}>
            {organisaatiot.map(this.renderOrganisaatio)}
          </select>
        </div>

        <div>
          <label htmlFor="kayttooikeudet">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA']}
          </label>
          <select onChange={this.selectKayttooikeudet} multiple 
            id="kayttooikeudet">
            {kayttooikeudet.map(this.renderKayttooikeus)}
          </select>
        </div>

        <a href="" onClick={this.addOrganisaatio}>
          {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_LISAA']}
        </a>

        <LisatytOrganisaatiot addedOrgs={this.props.addedOrgs} />

      </fieldset>
    )
  },

  addOrganisaatio: function(e) {
    e.preventDefault()

    if (this.state.selectedKayttooikeuses.length > 0) {
      organisations.add({
        id: this.state.selectedOrganisaatio,
        permissions: this.state.selectedKayttooikeuses
      })
    }
  },

  selectOrganisaatio: function(e) {
    this.setState({
      selectedOrganisaatio: e.target.value,
      selectedKayttooikeuses: []
    }) 
  },

  selectKayttooikeudet: function(e) {
    this.setState({
      selectedKayttooikeuses: Array.apply(null, e.target.options)
        .filter(option => option.selected)
        .map(option => option.value)
    })
  },

  renderOrganisaatio: function(org) {
    return (
      <option key={org.id} value={org.id}>
        {org['name-' + this.props.uiLang]}
      </option>  
    )
  },

  renderKayttooikeus: function(kayttooikeus) {
    return (
      <option key={kayttooikeus.id} value={kayttooikeus.id}>
        {kayttooikeus['name-' + this.props.uiLang]}
      </option>  
    )
  },

})

export default VirjailijanLisaysOrganisaatioon
