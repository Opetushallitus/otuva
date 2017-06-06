package fi.vm.sade.kayttooikeus.service;


public interface HenkiloCacheService {
    // Update only what has changed since last update
    void updateHenkiloCache();

    // Update whole cache
    void forceCleanUpdateHenkiloCache();
}
