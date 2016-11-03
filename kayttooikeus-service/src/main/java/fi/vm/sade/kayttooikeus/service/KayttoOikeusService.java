package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.repositories.dto.ExpiringKayttoOikeusDto;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.List;

public interface KayttoOikeusService {
    KayttoOikeusDto findKayttoOikeusById(long kayttoOikeusId);

    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<PalveluKayttoOikeusDto> listKayttoOikeusByPalvelu(String palveluName);

    List<KayttoOikeusHistoriaDto> listMyonnettyKayttoOikeusHistoriaForCurrentUser();

    List<ExpiringKayttoOikeusDto> findToBeExpiringMyonnettyKayttoOikeus(LocalDate at, Period... expirationPeriods);

    List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid);

    List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String oid, String organisaatioOid, String currentUserOid);

    List<MyonnettyKayttoOikeusDto> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String oid, String organisaatioOid);

    KayttoOikeusRyhmaDto findKayttoOikeusRyhma(long id);

    List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(long id);

    List<PalveluRooliDto> findPalveluRoolisByKayttoOikeusRyhma(long id);

    long createKayttoOikeusRyhma(KayttoOikeusRyhmaModifyDto uusiRyhma);

    long createKayttoOikeus(KayttoOikeusCreateDto kayttoOikeus);

    void updateKayttoOikeusForKayttoOikeusRyhma(long id, KayttoOikeusRyhmaModifyDto ryhmaData);
}
