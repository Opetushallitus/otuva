package fi.vm.sade.cas.oppija.service.impl;

import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.function.Function.identity;

@Service
public class PersonServiceImpl implements PersonService {

    private final OphHttpClient httpClient;
    private final OphProperties properties;

    public PersonServiceImpl(@Qualifier("oppijanumerorekisteriHttpClient") OphHttpClient httpClient, OphProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
    }

    @Override
    public Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber) {
        String url = properties.url("oppijanumerorekisteri-service.henkilo.byHetu.oid", nationalIdentificationNumber);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<String>execute(request)
                .expectedStatus(200)
                .mapWith(identity());
    }

}
