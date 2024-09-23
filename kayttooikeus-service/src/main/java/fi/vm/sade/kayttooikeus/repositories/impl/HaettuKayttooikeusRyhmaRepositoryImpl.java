package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaRepositoryCustom;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HaettuKayttooikeusRyhmaRepositoryImpl extends AbstractRepository implements HaettuKayttooikeusRyhmaRepositoryCustom {
    public List<HaettuKayttoOikeusRyhma> findBy(AnomusCriteria.AnomusCriteriaFunction<QAnomus, QKayttoOikeusRyhma, QHaettuKayttoOikeusRyhma> criteriaFunction,
                                                Long limit,
                                                Long offset,
                                                OrderByAnomus orderBy) {
        QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma = QHaettuKayttoOikeusRyhma.haettuKayttoOikeusRyhma;
        QAnomus qAnomus = QAnomus.anomus;
        QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;

        JPAQuery<HaettuKayttoOikeusRyhma> query = jpa()
                .select(qHaettuKayttoOikeusRyhma)
                .from(qHaettuKayttoOikeusRyhma)
                .leftJoin(qHaettuKayttoOikeusRyhma.anomus, qAnomus)
                .leftJoin(qHaettuKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);
        query.where(criteriaFunction.apply(qAnomus, qKayttoOikeusRyhma, qHaettuKayttoOikeusRyhma));
        query.where(qKayttoOikeusRyhma.passivoitu.isFalse());

        if (limit != null) {
            query.limit(limit);
        }
        if (offset != null) {
            query.offset(offset);
        }
        if (orderBy != null && orderBy.getValue() != null) {
            query.orderBy(orderBy.getValue());
        }
        return query.fetch();
    }

    @Override
    public List<HaettuKayttoOikeusRyhma> findBy(AnomusCriteria.AnomusCriteriaFunction<QAnomus, QKayttoOikeusRyhma, QHaettuKayttoOikeusRyhma> criteriaFunction) {
        return this.findBy(criteriaFunction, null, null, null);
    }

}
