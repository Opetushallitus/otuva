package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;

import java.util.List;
import java.util.Map;

public class CasOppijaAttributeReleasePolicy extends ReturnAllAttributeReleasePolicy {
    @Override
    protected Map<String, List<Object>> returnFinalAttributesCollection(Map<String, List<Object>> attributesToRelease, RegisteredService service) {
        // pac4j adds both saml name (e.g. urn:oid:2.5.4.3) and friendly name (e.g. cn) to principal attributes
        // and cas converts all attributes containing ":" into numbers which is not allowed in xml tags
        attributesToRelease.entrySet().removeIf(entry -> entry.getKey().contains(":"));
        return attributesToRelease;
    }
}
