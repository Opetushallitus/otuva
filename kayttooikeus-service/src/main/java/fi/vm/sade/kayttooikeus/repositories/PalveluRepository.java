package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Palvelu;

import java.util.List;

/**
 * Created by autio on 6.10.2016.
 */
public interface PalveluRepository {
    List<Palvelu> findAll();
}
