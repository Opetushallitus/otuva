import React from 'react'

import organisations from '../../logic/organisations'
import LisatytOrganisaatiot from '../LisatytOrganisaatiot/LisatytOrganisaatiot'

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

    const activeOrg = this.props.organisaatiot
      .filter(org => org.id === this.state.selectedOrganisaatio)
    const kayttooikeudet = activeOrg.length === 1 ? 
      activeOrg[0].permissions : this.props.organisaatiot[0].permissions

    return (
      <fieldset>
        <h2>{L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_OTSIKKO']}</h2>
        <div>
          <label htmlFor="org">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_ORGANISAATIO']}
          </label>
          <select id="org" onChange={this.selectOrganisaatio}>
            {this.props.organisaatiot.map(this.renderOrganisaatio)}
          </select>
        </div>
        <div>
          <label htmlFor="kayttooikeudet">
            {L['VIRKAILIJAN_LISAYS_ORGANISAATIOON_MYONNA_KAYTTOOIKEUKSIA']}
          </label>
          <select 
            onChange={this.selectKayttooikeudet} multiple id="kayttooikeudet">
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
    organisations.add({
      id: this.state.selectedOrganisaatio,
      permissions: this.state.selectedKayttooikeuses
    })
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
