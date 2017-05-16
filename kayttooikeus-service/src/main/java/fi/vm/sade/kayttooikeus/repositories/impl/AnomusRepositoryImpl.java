package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.model.QAnomus;
import fi.vm.sade.kayttooikeus.repositories.AnomusCriteria;
import fi.vm.sade.kayttooikeus.repositories.AnomusRepositoryCustom;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class AnomusRepositoryImpl implements AnomusRepositoryCustom {

    private final EntityManager entityManager;

    public AnomusRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(Anomus.class);
    }

    @Override
    public List<Anomus> findBy(AnomusCriteria criteria) {
        QAnomus qAnomus = QAnomus.anomus;

        return new JPAQuery<>(entityManager)
                .from(qAnomus)
                .where(criteria.condition(qAnomus))
                .select(qAnomus)
                .fetch();
    }

}
