import Bacon from 'baconjs'
import R from 'ramda'

import Dispatcher from './dispatcher'

const dispatcher = new Dispatcher()

const organisations = {
  toProperty: initialOrgs => {
    const addOrg = (orgs, newOrg) => {
      return R.find(R.propEq('id', newOrg.id))(orgs) ? 
        orgs : orgs.concat([newOrg])
    }

    const removeOrg = (orgs, orgIdToRemove) => {
      return R.reject(org => org.id === orgIdToRemove, orgs)
    }

    return Bacon.update(initialOrgs,
      [dispatcher.stream('add')], addOrg,
      [dispatcher.stream('remove')], removeOrg,
    )
  },
  add: org => dispatcher.push('add', org),
  removeById: orgId => dispatcher.push('remove', orgId),
}

export default organisations
