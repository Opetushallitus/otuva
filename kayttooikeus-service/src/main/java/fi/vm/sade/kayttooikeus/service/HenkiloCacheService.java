package fi.vm.sade.kayttooikeus.service;


import java.util.List;

public interface HenkiloCacheService {
    // Update only what has changed since last update
    void updateHenkiloCache();

    // Update whole cache
    void forceCleanUpdateHenkiloCacheInSingleTransaction();

    boolean saveAll(long offset, long count, List<String> oidHenkiloList);
}
