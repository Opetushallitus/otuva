package fi.vm.sade.kayttooikeus.repositories;

import java.util.Set;

public interface HenkiloViiteRepository {
    Set<String> getAllOidsForSamePerson(String oid);
}
