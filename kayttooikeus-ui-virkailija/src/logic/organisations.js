import Bacon from "baconjs";
import R from "ramda";
import {fetchOrganizationPermissionsPromise} from "../external/organisations";
import dispatcher from "./dispatcher";

const d = dispatcher();

const organisations = {
  toProperty: (initialOrgs=[]) => {
    const addOrgIfUnique = (orgs, newOrg) => {
      return R.find(R.propEq('oid', newOrg.oid))(orgs) ? orgs : [...orgs, newOrg]
    };
    const removeOrg = (orgs, orgOidToRemove) => {
      return R.reject(org => org.oid === orgOidToRemove, orgs)
    };
    const replaceByOid = (orgs, {oldOid, org}) => {
      const newAddedIndex = R.findIndex(R.pathEq(['oid'], org.oid))(orgs),
          newAddedAlready = newAddedIndex >= 0;
      const index = R.findIndex(R.pathEq(['oid'], oldOid))(orgs);
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
  removeByOid: orgId => d.push('remove', orgId),
  replaceByOid: (oldOid, org) => d.push('replace', {oldOid, org}),
  updated: () => d.push('updated', {})
};

export default organisations
const emptyOrganization = () => ({
  oid: '',
  organisation: {oid:''},
  selectablePermissions: [],
  selectedPermissions: []
});
export const addedOrganizationsP = organisations.toProperty([emptyOrganization()]);
export const addEmptyOrganization = () => organisations.add(emptyOrganization());
export const addSelectedOrganization = (organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      organisations.add({
        oid: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });
export const changeOrganization = (oldOid, organization, henkiloOid) =>
    fetchOrganizationPermissionsPromise(henkiloOid, organization.oid).then(permissions => {
      //console.info('changeOrganization replaceByOid', oldOid, 'new', organization, 'permissions', permissions);
      organisations.replaceByOid(oldOid, {
        oid: organization.oid,
        organisation: organization,
        selectablePermissions: permissions,
        selectedPermissions: []
      })
    });