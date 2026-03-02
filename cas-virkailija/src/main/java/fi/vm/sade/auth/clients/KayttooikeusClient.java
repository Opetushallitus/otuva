package fi.vm.sade.auth.clients;

import java.util.Optional;

import fi.vm.sade.auth.cas.CasUserAttributes;

public interface KayttooikeusClient {

    String getHenkiloOid(String username);

    String createLoginToken(String henkiloOid);

    Optional<String> getRedirectCodeByUsername(String username);

    Optional<CasUserAttributes> getUserAttributesByUsernamePassword(String username, String password);

    CasUserAttributes getHenkiloByAuthToken(String authToken);

    CasUserAttributes getUserAttributesByOid(String oid);

    CasUserAttributes getUserAttributesByIdpIdentifier(String idp, String identifier);

    CasUserAttributes getUserAttributesByHetu(String hetu);

    CasUserAttributes hakaRegistration(String temporaryToken, String identifier);
}
