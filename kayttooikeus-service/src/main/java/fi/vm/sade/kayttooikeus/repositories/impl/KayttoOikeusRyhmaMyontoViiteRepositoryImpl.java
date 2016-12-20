package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;

@Repository
public class KayttoOikeusRyhmaMyontoViiteRepositoryImpl
        extends BaseRepositoryImpl<KayttoOikeusRyhmaMyontoViite>
        implements KayttoOikeusRyhmaMyontoViiteRepository {

    @Override
    public List<Long> getSlaveIdsByMasterIds(List<Long> masterIds) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = kayttoOikeusRyhmaMyontoViite;

        if (CollectionUtils.isEmpty(masterIds)) {
            return new ArrayList<>();
        }

        return jpa().from(myontoViite)
                .where(myontoViite.masterId.in(masterIds))
                .distinct().select(myontoViite.slaveId).fetch();
    }

    @Override
    public boolean isCyclicMyontoViite(Long masterId, List<Long> slaveIds) {
        if (slaveIds.isEmpty()) {
            return false;
        }
        QKayttoOikeusRyhmaMyontoViite myontoViite = kayttoOikeusRyhmaMyontoViite;
        return exists(jpa().from(myontoViite)
                .where(
                        myontoViite.masterId.in(slaveIds),
                        myontoViite.slaveId.eq(masterId)
                ));
    }

    @Override
    public List<KayttoOikeusRyhmaMyontoViite> getMyontoViites(Long masterId) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = kayttoOikeusRyhmaMyontoViite;
        return jpa().from(myontoViite)
                .where(myontoViite.masterId.eq(masterId))
                .select(myontoViite)
                .fetch();
    }

}
