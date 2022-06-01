package fi.vm.sade.cas.oppija;

import java.util.Set;

public class CasOppijaConstants {

    public static final Set<String> SUPPORTED_LANGUAGES = Set.of("fi", "sv", "en");
    public static final String DEFAULT_LANGUAGE = "fi";

    public static final String ATTRIBUTE_NAME_NATIONAL_IDENTIFICATION_NUMBER = "nationalIdentificationNumber";
    public static final String ATTRIBUTE_NAME_PERSON_OID = "personOid";
    public static final String ATTRIBUTE_NAME_PERSON_NAME = "personName";

    public static final String AUTHENTICATION_ATTRIBUTE_CLIENT_PRINCIPAL_ID = "clientPrincipalId";

    public static final String REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT = "pac4jSamlLogout";
    public static final String TRANSITION_ID_IDP_LOGOUT = "IdpLogout";
    public static final String STATE_ID_IDP_LOGOUT = "IdpLogout";
    public static final boolean VALTUUDET_ENABLED = true;
}
