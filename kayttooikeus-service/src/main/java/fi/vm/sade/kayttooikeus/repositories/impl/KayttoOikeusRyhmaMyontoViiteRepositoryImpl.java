package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class KayttoOikeusRyhmaMyontoViiteRepositoryImpl extends AbstractRepository implements KayttoOikeusRyhmaMyontoViiteRepository {
    @Override
    public List<Long> getSlaveIdsByMasterIds(List<Long> masterIds) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;
        List<Long> slaveIds = null;

        List<KayttoOikeusRyhmaMyontoViite> viites = jpa().from(myontoViite)
                .where(myontoViite.masterId.in(masterIds))
                .distinct().select(myontoViite).fetch();

        if (viites != null && !viites.isEmpty()) {
            slaveIds = new ArrayList<Long>();
            for (KayttoOikeusRyhmaMyontoViite viite : viites) {
                slaveIds.add(viite.getSlaveId());
            }
        }

        return slaveIds;
    }
}
