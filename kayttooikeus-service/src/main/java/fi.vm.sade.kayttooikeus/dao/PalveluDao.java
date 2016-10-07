package fi.vm.sade.kayttooikeus.dao;

import fi.vm.sade.kayttooikeus.domain.Palvelu;

import java.util.List;

/**
 * Created by autio on 6.10.2016.
 */
public interface PalveluDao {
    List<Palvelu> findAll();
}
