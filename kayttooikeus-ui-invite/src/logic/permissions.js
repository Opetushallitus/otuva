import Bacon from 'baconjs'

import dispatcher from './dispatcher'

const d = dispatcher()

const permissions = {
  toProperty: (initialPermissions=[]) => {
    const set = (oldPermissions, newPermissions) => {
      return newPermissions
    }
    return Bacon.update(initialPermissions,
      [d.stream('set')], set,
    )
  },
  populateFromOrganisaatioOid: oid => {
    // TODO: use kayttooikeus-service
    return fetch(`/authentication-service/resources/kayttooikeusryhma/organisaatio/${oid}`, {
      credentials: 'same-origin'
    }).then(response => {
      return response.json()
    }).then(permissions => {
      d.push('set', permissions)
    })
  }
}

export default permissions
