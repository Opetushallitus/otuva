import Bacon from 'baconjs'
import R from 'ramda'
import {fetchOrganizationPermissionsPromise} from '../external/organisations'

import dispatcher from './dispatcher'

const d = dispatcher()

const organisations = {
  toProperty: (initialOrgs=[]) => {
    const addOrgIfUnique = (orgs, newOrg) => {
      return R.find(R.propEq('id', newOrg.id))(orgs) ? orgs : [...orgs, newOrg]
    };
    const removeOrg = (orgs, orgIdToRemove) => {
      return R.reject(org => org.id === orgIdToRemove, orgs)
    };
    const replaceById = (orgs, orgId, org) => {
      const index = R.findIndex(R.propEq('id', orgId))(orgs);
      if (index >= 0) {
        return [...orgs[0..index], org, ...orgs[index+1..orgs.length]];
      }
      return [...orgs, org];
    };
    return Bacon.update(initialOrgs,
      [d.stream('add')], addOrgIfUnique,
      [d.stream('remove')], removeOrg,
      [d.stream('replace')], replaceById
    )
  },
  add: org => d.push('add', org),
  removeById: orgId => d.push('remove', orgId),
  replaceById: (orgId, org) => d.push('replace', orgId, org)
};

export default organisations
export const addedOrganizationsP = organisations.toProperty();
export const addSelectedOrganization = (organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      organisations.add({
        id: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });
export const changeOrganization = (id, organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      organisations.replaceById(id, {
        id: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });