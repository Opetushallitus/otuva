package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.QHenkilo;
import fi.vm.sade.kayttooikeus.model.QKayttajatiedot;
import fi.vm.sade.kayttooikeus.repositories.HenkiloDataRepositoryCustom;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;

public class HenkiloDataRepositoryImpl implements HenkiloDataRepositoryCustom {

    private final EntityManager entityManager;

    public HenkiloDataRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(Henkilo.class);
    }

    @Override
    public Optional<Henkilo> findByKayttajatiedotUsername(String kayttajatunnus) {
        QHenkilo qHenkilo = QHenkilo.henkilo;
        QKayttajatiedot qKayttajatiedot = QKayttajatiedot.kayttajatiedot;

        Henkilo entity = new JPAQuery<>(entityManager)
                .from(qHenkilo)
                .join(qHenkilo.kayttajatiedot, qKayttajatiedot)
                .where(qKayttajatiedot.username.equalsIgnoreCase(kayttajatunnus))
                .select(qHenkilo)
                .fetchOne();
        return Optional.ofNullable(entity);
    }

}
