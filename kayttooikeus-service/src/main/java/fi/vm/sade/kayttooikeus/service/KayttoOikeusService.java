package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.List;

public interface KayttoOikeusService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser();

    List<ExpiringKayttoOikeusDto> findToBeExpiringMyonnettyKayttoOikeus(LocalDate at, Period... expirationPeriods);
}
