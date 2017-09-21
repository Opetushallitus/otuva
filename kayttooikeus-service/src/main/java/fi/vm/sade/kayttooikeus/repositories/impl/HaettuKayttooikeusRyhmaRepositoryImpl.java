package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.model.*;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaRepositoryCustom;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import java.util.List;
import javax.persistence.EntityManager;

import fi.vm.sade.kayttooikeus.service.external.OrganisaatioClient;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;

@Repository
public class HaettuKayttooikeusRyhmaRepositoryImpl implements HaettuKayttooikeusRyhmaRepositoryCustom {

    private final EntityManager entityManager;

    private final OrganisaatioClient organisaatioClient;

    public HaettuKayttooikeusRyhmaRepositoryImpl(JpaContext jpaContext, OrganisaatioClient organisaatioClient) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(HaettuKayttoOikeusRyhma.class);
        this.organisaatioClient = organisaatioClient;
    }

    @Override
    public List<HaettuKayttoOikeusRyhma> findBy(AnomusCriteria criteria, Long limit, Long offset, OrderByAnomus orderBy) {
        QHaettuKayttoOikeusRyhma qHaettuKayttoOikeusRyhma = QHaettuKayttoOikeusRyhma.haettuKayttoOikeusRyhma;
        QAnomus qAnomus = QAnomus.anomus;
        QKayttoOikeusRyhma qKayttoOikeusRyhma = QKayttoOikeusRyhma.kayttoOikeusRyhma;
        QKayttoOikeus qKayttoOikeus = QKayttoOikeus.kayttoOikeus;

        JPAQuery<HaettuKayttoOikeusRyhma> query = new JPAQuery<>(entityManager)
                .select(qHaettuKayttoOikeusRyhma)
                .from(qHaettuKayttoOikeusRyhma)
                .leftJoin(qHaettuKayttoOikeusRyhma.anomus, qAnomus)
                .leftJoin(qHaettuKayttoOikeusRyhma.kayttoOikeusRyhma, qKayttoOikeusRyhma);
        if (criteria.getAdminView() != null && criteria.getAdminView()) {
            query.leftJoin(qKayttoOikeusRyhma.kayttoOikeus, qKayttoOikeus);
        }
        query.where(criteria.condition(qAnomus, qKayttoOikeus, qHaettuKayttoOikeusRyhma, this.organisaatioClient));
        query.where(qKayttoOikeusRyhma.hidden.isFalse());

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

}
