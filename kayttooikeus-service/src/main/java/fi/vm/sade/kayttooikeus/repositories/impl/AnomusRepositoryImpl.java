package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import fi.vm.sade.kayttooikeus.repositories.AnomusRepositoryCustom;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;

@Repository
public class AnomusRepositoryImpl implements AnomusRepositoryCustom {

    private final EntityManager entityManager;

    public AnomusRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(Anomus.class);
    }

    @Override
    public List<Anomus> findBy(AnomusCriteria criteria) {
        return findBy(criteria, null, null);
    }

    @Override
    public List<Anomus> findBy(AnomusCriteria criteria, Long limit, Long offset) {
        QAnomus qAnomus = QAnomus.anomus;

        JPAQuery<Anomus> query = new JPAQuery<>(entityManager)
                .from(qAnomus)
                .where(criteria.condition(qAnomus))
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

}
