import Bacon from 'baconjs'
import R from 'ramda'

import dispatcher from './dispatcher'

const d = dispatcher()

const organisations = {
  toProperty: (initialOrgs=[]) => {
    const addOrgIfUnique = (orgs, newOrg) => {
      return R.find(R.propEq('id', newOrg.id))(orgs) ? orgs : [...orgs, newOrg]
    }
    const removeOrg = (orgs, orgIdToRemove) => {
      return R.reject(org => org.id === orgIdToRemove, orgs)
    }
    
    return Bacon.update(initialOrgs,
      [d.stream('add')], addOrgIfUnique,
      [d.stream('remove')], removeOrg,
    )
  },
  add: org => d.push('add', org),
  removeById: orgId => d.push('remove', orgId),
}

export default organisations
