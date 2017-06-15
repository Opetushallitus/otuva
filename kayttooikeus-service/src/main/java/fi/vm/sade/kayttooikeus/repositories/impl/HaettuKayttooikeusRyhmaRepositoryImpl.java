package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.HaettuKayttooikeusRyhmaRepositoryCustom;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class HaettuKayttooikeusRyhmaRepositoryImpl implements HaettuKayttooikeusRyhmaRepositoryCustom {

    private final EntityManager entityManager;

    public HaettuKayttooikeusRyhmaRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(HaettuKayttoOikeusRyhma.class);
    }

}
