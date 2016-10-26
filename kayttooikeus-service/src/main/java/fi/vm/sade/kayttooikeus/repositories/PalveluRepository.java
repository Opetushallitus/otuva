package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.PalveluDto;
import fi.vm.sade.kayttooikeus.model.Palvelu;

import java.util.List;

public interface PalveluRepository {
    List<PalveluDto> findAll();

    List<Palvelu> findByKayttoOikeusIds(List<Long> koIds);

    List<Palvelu> findByName(String name);
}
