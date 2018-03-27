package fi.vm.sade.kayttooikeus.repositories.impl;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.KayttoOikeusRyhmaMyontoViiteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeusRyhmaMyontoViite.kayttoOikeusRyhmaMyontoViite;
import fi.vm.sade.kayttooikeus.repositories.criteria.MyontooikeusCriteria;
import java.util.Map;
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
    public Map<String, Set<Long>> getSlaveIdsByMasterHenkiloOid(String henkiloOid, MyontooikeusCriteria criteria) {
        QKayttoOikeusRyhmaMyontoViite myontoViite = kayttoOikeusRyhmaMyontoViite;
        QKayttoOikeusRyhma kayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QMyonnettyKayttoOikeusRyhmaTapahtuma myonnettyKayttoOikeusRyhmaTapahtuma = QMyonnettyKayttoOikeusRyhmaTapahtuma.myonnettyKayttoOikeusRyhmaTapahtuma;
        QOrganisaatioHenkilo organisaatioHenkilo = QOrganisaatioHenkilo.organisaatioHenkilo;
        QHenkilo henkilo = QHenkilo.henkilo;

        JPAQuery<?> query = jpa()
                .from(myontoViite, myonnettyKayttoOikeusRyhmaTapahtuma)
                .join(myonnettyKayttoOikeusRyhmaTapahtuma.kayttoOikeusRyhma, kayttoOikeusRyhma)
                .join(myonnettyKayttoOikeusRyhmaTapahtuma.organisaatioHenkilo, organisaatioHenkilo)
                .join(organisaatioHenkilo.henkilo, henkilo)
                .where(kayttoOikeusRyhma.id.eq(myontoViite.masterId))
                .where(henkilo.oidHenkilo.eq(henkiloOid))
                .distinct();

        if (criteria.getPalvelut() != null || criteria.getRooli() != null) {
            QKayttoOikeus kayttoOikeus = QKayttoOikeus.kayttoOikeus;
            query.join(kayttoOikeusRyhma.kayttoOikeus, kayttoOikeus);

            if (criteria.getPalvelut() != null) {
                QPalvelu palvelu = QPalvelu.palvelu;
                query.join(kayttoOikeus.palvelu, palvelu);
                query.where(palvelu.name.in(criteria.getPalvelut()));
            }
            if (criteria.getRooli() != null) {
                query.where(kayttoOikeus.rooli.eq(criteria.getRooli()));
            }
        }

        return query.transform(groupBy(organisaatioHenkilo.organisaatioOid).as(set(myontoViite.slaveId)));
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
