package fi.vm.sade.cas.oppija.service.impl;

import fi.vm.sade.cas.oppija.exception.SystemException;
import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.cas.oppija.service.impl.PersonServiceImpl.OidByEidasIdRequest;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.properties.OphProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static fi.vm.sade.cas.oppija.controller.ControllerUtils.ioExceptionToSystemException;
import static java.util.function.Function.identity;

@Service
public class PersonServiceImpl implements PersonService {

    private final OphHttpClient httpClient;
    private final OphProperties properties;
    private final ObjectMapper objectMapper;

    public PersonServiceImpl(@Qualifier("oppijanumerorekisteriHttpClient") OphHttpClient httpClient, OphProperties properties, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> findOidByNationalIdentificationNumber(String nationalIdentificationNumber) {
        String url = properties.url("oppijanumerorekisteri-service.henkilo.byHetu.oid", nationalIdentificationNumber);
        OphHttpRequest request = OphHttpRequest.Builder.get(url).build();
        return httpClient.<String>execute(request)
                .expectedStatus(200)
                .mapWith(identity());
    }

    @Override
    public Optional<String> findOidByEidasId(String eidasId) throws SystemException {
        String url = properties.url("oppijanumerorekisteri-service.henkilo.byEidas.oid");
        OphHttpRequest request = OphHttpRequest.Builder
                .post(url)
                .setEntity(new OphHttpEntity.Builder()
                        .content(ioExceptionToSystemException(() -> objectMapper.writeValueAsString(new OidByEidasIdRequest(eidasId))))
                        .contentType(ContentType.APPLICATION_JSON)
                        .build())
                .build();
        return httpClient.<String>execute(request)
                .expectedStatus(200)
                .mapWith(identity());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    class OidByEidasIdRequest {
        public String eidasId;
    }
}
