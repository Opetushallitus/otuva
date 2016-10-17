package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.repositories.PalveluRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.model.QPalvelu.palvelu;

/**
 * Created by autio on 6.10.2016.
 */
@Repository
public class PalveluRepositoryImpl extends AbstractRepository implements PalveluRepository {
    @Override
    public List<Palvelu> findAll() {
        return from(palvelu).select(palvelu).fetch();
    }
}
