package fi.vm.sade.kayttooikeus.dto.permissioncheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PermissionCheckRequestDto {

    private List<String> personOidsForSamePerson;
    private List<String> organisationOids = new ArrayList<String>();
    private Set<String> loggedInUserRoles;

    public List<String> getPersonOidsForSamePerson() {
        return personOidsForSamePerson;
    }

    public void setPersonOidsForSamePerson(List<String> personOidsForSamePerson) {
        this.personOidsForSamePerson = personOidsForSamePerson;
    }

    public List<String> getOrganisationOids() {
        return organisationOids;
    }

    public void setOrganisationOids(List<String> organisationOids) {
        this.organisationOids = organisationOids;
    }

    public Set<String> getLoggedInUserRoles() {
        return loggedInUserRoles;
    }

    public void setLoggedInUserRoles(Set<String> loggedInUserRoles) {
        this.loggedInUserRoles = loggedInUserRoles;
    }
}
