package fi.vm.sade.cas.oppija.service;

import java.util.Optional;

import fi.vm.sade.cas.oppija.exception.SystemException;

public interface PersonService {

    Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber);

    Optional<String> findOidByEidasTunniste(String eidasTunniste) throws SystemException;

}
