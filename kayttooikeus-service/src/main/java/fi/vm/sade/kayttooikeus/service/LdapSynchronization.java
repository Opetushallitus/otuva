package fi.vm.sade.kayttooikeus.service;


public interface LdapSynchronization {
    void updateAccessRightGroup(Long id);
    void updateHenkilo(String henkiloOid, int priority);
}
