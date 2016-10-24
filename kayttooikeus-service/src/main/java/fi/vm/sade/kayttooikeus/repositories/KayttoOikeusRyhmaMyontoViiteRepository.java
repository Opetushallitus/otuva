package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;

import java.util.List;

public interface KayttoOikeusRyhmaMyontoViiteRepository {
    List<Long> getSlaveIdsByMasterIds(List<Long> masterIds);

    KayttoOikeusRyhmaMyontoViite insert(KayttoOikeusRyhmaMyontoViite myontoViite);

    boolean checkCyclicMyontoViite(Long id, List<Long> slaveIds);

    List<KayttoOikeusRyhmaMyontoViite> getMyontoViites(Long id);

    void delete(KayttoOikeusRyhmaMyontoViite viite);
}
