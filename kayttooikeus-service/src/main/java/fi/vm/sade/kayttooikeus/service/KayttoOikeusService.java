package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusHistoriaDto;
import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.dto.PalveluKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaModifyDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.List;

public interface KayttoOikeusService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser();

    List<ExpiringKayttoOikeusDto> findToBeExpiringMyonnettyKayttoOikeus(LocalDate at, Period... expirationPeriods);

    List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid);

    List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String oid, String organisaatioOid, String currentUserOid);

    List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String oid, String organisaatioOid);

    KayttoOikeusRyhmaDto findKayttoOikeusRyhma(Long id);

    List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(Long id);

    List<PalveluRoooliDto> findPalveluRoolisByKayttoOikeusRyhma(Long id);

    KayttoOikeusRyhmaDto createKayttoOikeusRyhma(KayttoOikeusRyhmaModifyDto uusiRyhma);

    KayttoOikeusDto createKayttoOikeus(KayttoOikeusDto kayttoOikeus);

    KayttoOikeusRyhmaDto updateKayttoOikeusForKayttoOikeusRyhma(Long id, KayttoOikeusRyhmaModifyDto ryhmaData);
}
