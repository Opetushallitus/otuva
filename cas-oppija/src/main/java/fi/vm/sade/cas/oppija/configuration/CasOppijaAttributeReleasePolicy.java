package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;

import java.util.List;
import java.util.Map;

public class CasOppijaAttributeReleasePolicy extends ReturnAllAttributeReleasePolicy {
    private final List<String> attributes = List.of(
        "cn",
        "displayName",
        "firstName",
        "givenName",
        "KotikuntaKuntanumero",
        "KotikuntaKuntaR",
        "KotikuntaKuntaS",
        "mail",
        "nationalIdentificationNumber",
        "notBefore",
        "notOnOrAfter",
        "personName",
        "personOid",
        "sn",
        "TurvakieltoTieto",
        "VakinainenKotimainenLahiosoitePostinumero",
        "VakinainenKotimainenLahiosoitePostitoimipaikkaR",
        "VakinainenKotimainenLahiosoitePostitoimipaikkaS",
        "VakinainenKotimainenLahiosoiteR",
        "VakinainenKotimainenLahiosoiteS",
        "VakinainenUlkomainenLahiosoite",
        "VakinainenUlkomainenLahiosoitePaikkakuntaJaValtioS",
        "VakinainenUlkomainenLahiosoitePaikkakuntaJaValtioR",
        "VakinainenUlkomainenLahiosoitePaikkakuntaJaValtioSelvakielinen",
        "vtjVerified"
    );

    @Override
    protected Map<String, List<Object>> returnFinalAttributesCollection(Map<String, List<Object>> attributesToRelease, RegisteredService service) {
        attributesToRelease.entrySet().removeIf(entry -> !attributes.contains(entry.getKey()));
        return attributesToRelease;
    }
}
