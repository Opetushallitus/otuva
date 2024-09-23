package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import fi.vm.sade.kayttooikeus.repositories.AnomusRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Repository
public class AnomusRepositoryImpl extends AbstractRepository implements AnomusRepositoryCustom {
    @Override
    public List<Anomus> findBy(Function<QAnomus, Predicate> criteria) {
        return findBy(criteria, null, null);
    }

    @Override
    public List<Anomus> findBy(Function<QAnomus, Predicate> criteria, Long limit, Long offset) {
        QAnomus qAnomus = QAnomus.anomus;

        JPAQuery<Anomus> query = jpa()
                .from(qAnomus)
                .where(criteria.apply(qAnomus))
                .select(qAnomus)
                .orderBy(qAnomus.anomusTilaTapahtumaPvm.desc());
        if (limit != null) {
            query.limit(limit);
        }
        if (offset != null) {
            query.offset(offset);
        }
        return query.fetch();
    }

    @Override
    public Collection<Anomus> findExpired(Period threshold) {
        QAnomus anomus = QAnomus.anomus;
        return jpa()
                .from(anomus)
                .select(anomus)
                .where(
                        anomus.anomuksenTila.eq(AnomuksenTila.ANOTTU)
                                .and(anomus.anottuPvm.lt(LocalDateTime.now().minus(threshold))))
                .fetch();
    }
}
