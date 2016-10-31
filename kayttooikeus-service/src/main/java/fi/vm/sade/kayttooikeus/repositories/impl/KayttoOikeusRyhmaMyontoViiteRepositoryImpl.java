package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
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
            slaveIds = new ArrayList<>();
            for (KayttoOikeusRyhmaMyontoViite viite : viites) {
                slaveIds.add(viite.getSlaveId());
            }
        }

        return slaveIds;
    }

    @Override
    public KayttoOikeusRyhmaMyontoViite insert(KayttoOikeusRyhmaMyontoViite myontoViite) {
        return persist(myontoViite);
    }

    @Override
    public boolean checkCyclicMyontoViite(Long masterId, List<Long> slaveIds) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;
        // If the given master ID - slave ID combo is found even once from the references,
        // that would create cyclic master-slave relationship which MUST NO be allowed!!
        Long count = jpa().from(myontoViite)
                .where(
                        myontoViite.masterId.in(slaveIds),
                        myontoViite.slaveId.eq(masterId)
                )
                .fetchCount();

        return count > 0;
    }

    @Override
    public List<KayttoOikeusRyhmaMyontoViite> getMyontoViites(Long masterId) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;
        return jpa().from(myontoViite)
                .where(myontoViite.masterId.eq(masterId))
                .select(myontoViite)
                .fetch();
    }

    @Override
    public void delete(KayttoOikeusRyhmaMyontoViite viite) {
        remove(viite);
    }

}
