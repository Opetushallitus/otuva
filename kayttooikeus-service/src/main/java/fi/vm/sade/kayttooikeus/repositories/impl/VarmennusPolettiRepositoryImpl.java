package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.VarmennusPoletti;
import fi.vm.sade.kayttooikeus.repositories.VarmennusPolettiRepositoryCustom;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class VarmennusPolettiRepositoryImpl implements VarmennusPolettiRepositoryCustom {

    private final EntityManager entityManager;

    public VarmennusPolettiRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(VarmennusPoletti.class);
    }

}
