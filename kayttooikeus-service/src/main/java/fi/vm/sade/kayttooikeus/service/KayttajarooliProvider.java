package fi.vm.sade.kayttooikeus.service;

import java.util.Map;
import java.util.Set;

public interface KayttajarooliProvider {

    Set<String> getByKayttajaOid(String kayttajaOid);

    Map<String, Set<String>> getRolesByOrganisation(String kayttajaOid);

}
