package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;
import fi.vm.sade.kayttooikeus.model.Henkilo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface IdentificationService {
    String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier);

    @Transactional
    String generateAuthTokenForHenkilo(Henkilo henkilo, String idpKey, String idpIdentifier);

    String getHenkiloOidByIdpAndIdentifier(String idpKey, String idpIdentifier);

    IdentifiedHenkiloTypeDto findByTokenAndInvalidateToken(String authToken);

    String updateIdentificationAndGenerateTokenForHenkiloByHetu(String hetu);

    Set<String> getHakatunnuksetByHenkiloAndIdp(String oid, String idpKey);

    Set<String> updateHakatunnuksetByHenkiloAndIdp(String oid, Set<String> hakatunnisteet);

    String updateKutsuAndGenerateTemporaryKutsuToken(String kutsuToken, String hetu, String etunimet, String sukunimi);

    String createLoginToken(String oidHenkilo);

    String handleStrongIdentification(String hetu, String etunimet, String sukunimi, String loginToken);
}
