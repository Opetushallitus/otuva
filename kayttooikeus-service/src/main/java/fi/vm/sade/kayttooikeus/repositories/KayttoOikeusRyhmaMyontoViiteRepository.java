package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;

import java.util.List;
import java.util.Set;

public interface KayttoOikeusRyhmaMyontoViiteRepository extends BaseRepository<KayttoOikeusRyhmaMyontoViite> {

    Set<Long> getMasterIdsBySlaveIds(Set<Long> slaveIds);

    List<Long> getSlaveIdsByMasterIds(List<Long> masterIds);

    boolean isCyclicMyontoViite(Long id, List<Long> slaveIds);

    List<KayttoOikeusRyhmaMyontoViite> getMyontoViites(Long id);
}
