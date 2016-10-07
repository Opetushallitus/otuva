package fi.vm.sade.kayttooikeus.dao.impl;

import fi.vm.sade.kayttooikeus.dao.PalveluDao;
import fi.vm.sade.kayttooikeus.domain.Palvelu;
import org.springframework.stereotype.Repository;

import java.util.List;

import static fi.vm.sade.kayttooikeus.domain.QPalvelu.palvelu;

/**
 * Created by autio on 6.10.2016.
 */
@Repository
public class PalveluDaoImpl extends AbstractDao implements PalveluDao {
    @Override
    public List<Palvelu> findAll() {
        return from(palvelu).list(palvelu);
    }
}
