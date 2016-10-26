package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.List;

public interface KayttoOikeusRepository {
    boolean isHenkiloMyonnettyKayttoOikeusToPalveluInRole(String henkiloOid, String palvelu, String role);

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForHenkilo(String henkiloOid);

    List<ExpiringKayttoOikeusDto> findSoonToBeExpiredTapahtumas(LocalDate now, Period...expireThresholds);

    List<KayttoOikeus> findByKayttoOikeusRyhma(Long id);

    List<Long> findByKayttoOikeusRyhmaIds(Long id);

    KayttoOikeus findByRooliAndPalvelu(KayttoOikeus kayttoOikeus);

    KayttoOikeus insert(KayttoOikeus kayttoOikeus);
}
