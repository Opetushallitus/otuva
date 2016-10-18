package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.repositories.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.repositories.dto.PalveluKayttoOikeusDto;

import java.util.List;

public interface KayttoOikeusRepository {
    boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palvelu, String role);

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForHenkilo(String henkiloOid);
}
