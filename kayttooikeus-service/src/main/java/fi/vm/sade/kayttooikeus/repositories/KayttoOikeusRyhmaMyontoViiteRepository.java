package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;

import java.util.List;

public interface KayttoOikeusRyhmaMyontoViiteRepository extends BaseRepository<KayttoOikeusRyhmaMyontoViite> {
    List<Long> getSlaveIdsByMasterIds(List<Long> masterIds);

    boolean isCyclicMyontoViite(Long id, List<Long> slaveIds);

    List<KayttoOikeusRyhmaMyontoViite> getMyontoViites(Long id);
}
