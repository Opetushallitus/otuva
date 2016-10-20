package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;

import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
public interface KayttoOikeusRyhmaService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid);

    List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String oid, String organisaatioOid, String currentUserOid);

    List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(String oid, String organisaatioOid);

    KayttoOikeusRyhmaDto findKayttoOikeusRyhma(Long id);

    List<KayttoOikeusRyhmaDto> findSubRyhmasByMasterRyhma(Long id);

    List<PalveluRoooliDto> findKayttoOikeusByKayttoOikeusRyhma(Long id);
}
