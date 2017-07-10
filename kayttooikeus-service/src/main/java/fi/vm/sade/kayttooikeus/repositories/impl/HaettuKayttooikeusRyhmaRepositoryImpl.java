package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import fi.vm.sade.kayttooikeus.model.QHaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaRepositoryCustom;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class HaettuKayttooikeusRyhmaRepositoryImpl implements HaettuKayttooikeusRyhmaRepositoryCustom {

    private final EntityManager entityManager;

    public HaettuKayttooikeusRyhmaRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(HaettuKayttoOikeusRyhma.class);
    }

    @Override
    public List<HaettuKayttoOikeusRyhma> findBy(AnomusCriteria criteria, Long limit, Long offset, OrderByAnomus orderBy) {
        QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma = QHaettuKayttoOikeusRyhma.haettuKayttoOikeusRyhma;
        QAnomus qAnomus = QAnomus.anomus;

        JPAQuery<HaettuKayttoOikeusRyhma> query = new JPAQuery<>(entityManager)
                .from(qHaettuKayttoOikeusRyhma)
                .join(qHaettuKayttoOikeusRyhma.anomus, qAnomus)
                .where(criteria.condition(qAnomus))
                .select(qHaettuKayttoOikeusRyhma);
        if (limit != null) {
            query.limit(limit);
        }
        if (offset != null) {
            query.offset(offset);
        }
        if(orderBy != null && orderBy.getValue() != null) {
            query.orderBy(orderBy.getValue());
        }
        return query.fetch();
    }

}
