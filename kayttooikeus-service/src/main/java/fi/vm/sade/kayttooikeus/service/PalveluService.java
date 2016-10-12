package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.service.dto.PalveluDto;

import java.util.List;

/**
 * Created by autio on 6.10.2016.
 */
public interface PalveluService {
    List<PalveluDto> listPalvelus();
}
