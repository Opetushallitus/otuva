package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

@Repository
public class KayttoOikeusRyhmaMyontoViiteRepositoryImpl
        extends BaseRepositoryImpl<KayttoOikeusRyhmaMyontoViite>
        implements KayttoOikeusRyhmaMyontoViiteRepository {

    @Override
    public Set<Long> getMasterIdsBySlaveIds(Set<Long> slaveIds) {
        QKayttoOikeusRyhmaMyontoViite qMyontoViite = kayttoOikeusRyhmaMyontoViite;

        return jpa().from(qMyontoViite)
                .where(qMyontoViite.slaveId.in(slaveIds))
                .distinct().select(qMyontoViite.masterId).fetch().stream().collect(toSet());
    }

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
    public List<Long> getSlaveIdsByMasterHenkiloOid(String oid) {

        QKayttoOikeusRyhmaMyontoViite myontoViite = kayttoOikeusRyhmaMyontoViite;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;

        return jpa()
                .from(myontoViite, myonnettyKayttoOikeusRyhmaTapahtuma)
                .join(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .join(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .where(organisaatioHenkilo.henkilo.oidHenkilo.eq(oid)
                    .and(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma.eq(kayttoOikeusRyhma))
                    .and(kayttoOikeusRyhma.id.eq(myontoViite.masterId)))
                .distinct()
                .select(myontoViite.slaveId)
                .fetch();
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
