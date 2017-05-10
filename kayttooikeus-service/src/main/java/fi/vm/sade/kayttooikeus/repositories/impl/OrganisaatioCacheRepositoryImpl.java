package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioCacheRepositoryCustom;
import java.util.Collection;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class OrganisaatioCacheRepositoryImpl implements OrganisaatioCacheRepositoryCustom {

    private final EntityManager entityManager;

    public OrganisaatioCacheRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(OrganisaatioCache.class);
    }

    @Override
    public void persistInBatch(Collection<OrganisaatioCache> entities, int batchSize) {
        int i = 0;
        for (OrganisaatioCache entity : entities) {
            i++;
            entityManager.persist(entity);
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        if (i % batchSize != 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }

}
