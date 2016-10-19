package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;

import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
public interface KayttoOikeusRyhmaService {
    List<KayttoOikeusRyhmaDto> listAllKayttoOikeusRyhmas();

    List<KayttoOikeusRyhmaDto> listPossibleRyhmasByOrganization(String organisaatioOid);

    List<MyonnettyKayttoOikeusDTO> listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(String oid, String organisaatioOid, String currentUserOid);
}
