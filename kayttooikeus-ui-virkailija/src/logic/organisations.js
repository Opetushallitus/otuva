import Bacon from "baconjs";
import R from "ramda";
import {fetchOrganizationPermissionsPromise} from "../external/organisations";
import dispatcher from "./dispatcher";

const d = dispatcher()

const organisations = {
  toProperty: (initialOrgs=[]) => {
    const addOrgIfUnique = (orgs, newOrg) => {
      return R.find(R.propEq('id', newOrg.id))(orgs) ? orgs : [...orgs, newOrg]
    };
    const removeOrg = (orgs, orgIdToRemove) => {
      return R.reject(org => org.id === orgIdToRemove, orgs)
    };
    const replaceByOid = (orgs, {oldOid, org}) => {
      const newAddedIndex = R.findIndex(R.pathEq(['id'], org.id))(orgs),
          newAddedAlready = newAddedIndex >= 0;
      const index = R.findIndex(R.pathEq(['id'], oldOid))(orgs);
      if (index >= 0) {
        const orgsCopy = [...orgs];
        if (newAddedAlready) {
          orgsCopy.splice(index,1);
        } else {
          orgsCopy.splice(index,1,org);
        }
        return orgsCopy;
      }
      return newAddedAlready ? orgs : [...orgs, org];
    };
    return Bacon.update(initialOrgs,
      [d.stream('add')], addOrgIfUnique,
      [d.stream('remove')], removeOrg,
      [d.stream('replace')], replaceByOid,
      [d.stream('updated')], orgs => ([...orgs])
    )
  },
  add: org => d.push('add', org),
  removeById: orgId => d.push('remove', orgId),
  replaceByOid: (oldOid, org) => d.push('replace', {oldOid, org}),
  updated: () => d.push('updated', {})
};

export default organisations
const emptyOrganization = () => ({
  id: '',
  organisation: {oid:''},
  selectablePermissions: [],
  selectedPermissions: []
});
export const addedOrganizationsP = organisations.toProperty([emptyOrganization()]);
export const addEmptyOrganization = () => organisations.add(emptyOrganization());
export const addSelectedOrganization = (organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      organisations.add({
        id: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });
export const changeOrganization = (oldOid, organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      organisations.replaceByOid(oldOid, {
        id: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });