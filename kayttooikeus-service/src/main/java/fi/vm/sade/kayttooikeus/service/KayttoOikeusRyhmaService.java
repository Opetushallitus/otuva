package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.service.dto.*;

import java.util.List;

public interface KayttoOikeusRyhmaService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid);

    List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String oid, String organisaatioOid, String currentUserOid);

    List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String oid, String organisaatioOid);

    KayttoOikeusRyhmaDto findKayttoOikeusRyhma(Long id);

    List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(Long id);

    List<PalveluRoooliDto> findPalveluRoolisByKayttoOikeusRyhma(Long id);

    KayttoOikeusRyhmaDto createKayttoOikeusRyhma(KayttoOikeusRyhmaModifyDto uusiRyhma);

    KayttoOikeusDto createKayttoOikeus(KayttoOikeusDto kayttoOikeus);

    KayttoOikeusRyhmaDto updateKayttoOikeusForKayttoOikeusRyhma(Long id, KayttoOikeusRyhmaModifyDto ryhmaData);
}
