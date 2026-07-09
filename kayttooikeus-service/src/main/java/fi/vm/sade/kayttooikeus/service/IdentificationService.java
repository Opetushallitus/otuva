package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;
import fi.vm.sade.kayttooikeus.model.TunnistusToken;

import java.util.Set;

public interface IdentificationService {
    String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier);

    String generateAuthTokenForHenkilo(Henkilo henkilo, String idpKey, String idpIdentifier);

    String getHenkiloOidByIdpAndIdentifier(String idpKey, String idpIdentifier);

    Identification findByTokenAndInvalidateToken(String authToken);

    Set<String> getTunnisteetByHenkiloAndIdp(String identityProvider, String oid);
    Set<String> updateTunnisteetByHenkiloAndIdp(String identityProvider, String oid, Set<String> hakatunnisteet);

    String createLoginToken(String oidHenkilo, Boolean salasananVaihto, String hetu);

    TunnistusToken getByValidLoginToken(String loginToken);

    String consumeLoginToken(String loginToken, String identification_idp);

    String getIdpEntityIdForCurrentSession();
}
