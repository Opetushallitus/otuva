package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.repositories.PalveluRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QKayttoOikeus.kayttoOikeus;
import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

@Repository
public class PalveluRepositoryImpl extends AbstractRepository implements PalveluRepository {
    @Override
    public List<Palvelu> findAll() {
        return from(palvelu).select(palvelu).fetch();
    }

    @Override
    public List<Palvelu> findByKayttoOikeusIds(List<Long> koIds) {
        return jpa().from(palvelu)
                .innerJoin(palvelu.kayttoOikeus, kayttoOikeus).fetchJoin()
                .distinct()
                .where(kayttoOikeus.id.in(koIds))
                .select(palvelu)
                .fetch();
    }
}
