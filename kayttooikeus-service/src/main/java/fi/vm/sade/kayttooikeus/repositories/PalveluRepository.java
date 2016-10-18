package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Palvelu;

import java.util.List;

public interface PalveluRepository {
    List<Palvelu> findAll();
}
