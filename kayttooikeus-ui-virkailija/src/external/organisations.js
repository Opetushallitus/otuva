import Bacon from "baconjs";
import R from "ramda";
import {omaOidP} from "./omattiedot";
import {handleFetchError} from "../logic/fetchUtils";
import {toLocalizedText} from '../logic/localizabletext'
import {localeP} from './l10n'

const orgsResponseS = omaOidP.changes().flatMap(henkiloOid =>
    Bacon.fromPromise(fetch(window.url('kayttooikeus-service.henkilo.organisaatios', henkiloOid), {credentials: 'same-origin'})
        .then(handleFetchError).then(response => response.json())));
export default orgsResponseS
export const orgsP = orgsResponseS.toProperty();
export const organizationHierarchyRootsP = Bacon.combineWith(orgsP, localeP, (orgs, locale) => {
    // First sort by name (thus will be sorted in every level):
    orgs = R.sortBy(org => toLocalizedText(locale, org.organisaatio.nimi), orgs);
    const byOid = {};
    var lowestLevel = null;
    // Determine organization levels, lowest level, direct parent oid and map by oid:
    R.forEach(org => {
        byOid[org.organisaatio.oid] = org;
        if (!org.organisaatio.parentOidPath) {
            org.organisaatio.level = 1; // root
            org.organisaatio.parentOid = null;
        } else {
            const parents = org.organisaatio.parentOidPath.split('/');
            org.organisaatio.level = parents.length;
            org.organisaatio.parentOid = parents[1];
        }
        if (lowestLevel === null || lowestLevel > org.organisaatio.level) {
            lowestLevel = org.organisaatio.level;
        }
        org.organisaatio.children = [];
    }, orgs);
    // Map children by direct parent:
    const roots = [];
    R.forEach(org => {
        if (org.organisaatio.parentOid) {
            const parent = byOid[org.organisaatio.parentOid];
            if (parent) {
                parent.organisaatio.children.push(org);
            } else {
                // not the root org but root can not be found (=> makes this lowest accessable)
                roots.push(org);
            }
        } else {
            // root org:
            roots.push(org);
        }
    }, orgs);
    return roots;
});
export const organizationsFlatInHierarchyOrderP = Bacon.combineWith(organizationHierarchyRootsP, organizationHierarchyRoots => {
    const result = [];
    const map = org => {
        result.push(org);
        if (org.organisaatio.children) {
            org.organisaatio.children.map(map);
        }
    };
    R.forEach(map, organizationHierarchyRoots);
    return result;
});

export const fetchOrganizationPermissionsPromise = (henkiloOid, organisaatioOid) =>
    fetch(window.url('kayttooikeus-service.kayttooikeusryhma.forHenkilo.inOrganisaatio', henkiloOid, organisaatioOid), {
        credentials: 'same-origin'}).then(handleFetchError).then(response => response.json());