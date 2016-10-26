package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.PalveluDto;

import java.util.List;

public interface PalveluRepository {
    List<PalveluDto> findAll();
}
