package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.repositories.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.repositories.dto.PalveluKayttoOikeusDto;

import java.util.List;

public interface KayttoOikeusService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser();
}
