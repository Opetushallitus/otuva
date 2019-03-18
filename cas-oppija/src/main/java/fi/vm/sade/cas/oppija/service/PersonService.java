package fi.vm.sade.cas.oppija.service;

import java.util.Optional;

public interface PersonService {

    Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber);

}
