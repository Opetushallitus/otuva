package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.HenkiloHakaDto;
import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.model.Identification;

import java.util.List;

public interface IdentificationService {
    String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier);

    String getHenkiloOidByIdpAndIdentifier(String idpKey, String idpIdentifier);

    IdentifiedHenkiloTypeDto findByTokenAndInvalidateToken(String authToken);

    String updateIdentificationAndGenerateTokenForHenkiloByHetu(String hetu);

    List<HenkiloHakaDto> getHenkiloHakaDTOsByHenkiloAndIdp(String oid, String idpKey);
}
