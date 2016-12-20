package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.IdentifiedHenkiloTypeDto;

public interface IdentificationService {
    String generateAuthTokenForHenkilo(String oid, String idpKey, String idpIdentifier);

    String getHenkiloOidByIdpAndIdentifier(String idpKey, String idpIdentifier);

    IdentifiedHenkiloTypeDto validateAuthToken(String authToken);

    String generateTokenForHenkilo(String henkiloOid);
}
