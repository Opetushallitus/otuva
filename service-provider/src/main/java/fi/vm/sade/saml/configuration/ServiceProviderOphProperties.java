package fi.vm.sade.saml.configuration;

import fi.vm.sade.properties.OphProperties;
import java.nio.file.Paths;

public class ServiceProviderOphProperties extends OphProperties {

    public ServiceProviderOphProperties() {
        addFiles("/service-provider-oph.properties");
        addOptionalFiles(Paths.get("/app/config/service-provider.properties").toString());
    }

}
