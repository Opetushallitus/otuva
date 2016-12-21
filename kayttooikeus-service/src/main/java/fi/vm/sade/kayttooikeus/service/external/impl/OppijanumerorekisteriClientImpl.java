package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.ServiceUsersProperties;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.FunctionalUtils;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkilonYhteystiedotViewDto;
import fi.vm.sade.properties.OphProperties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Component
public class OppijanumerorekisteriClientImpl implements OppijanumerorekisteriClient {
    private static final String SERVICE_CODE = "kayttooikeus.kayttooikeuspalvelu-service";
    private final ObjectMapper objectMapper;
    private final OphProperties urlProperties;
    private final CachingRestClient restClient;
    private final CachingRestClient serviceAccountClient;

    @Autowired
    public OppijanumerorekisteriClientImpl(ObjectMapper objectMapper, OphProperties urlProperties,
                                           ServiceUsersProperties serviceUsersProperties) {
        this.objectMapper = objectMapper;
        this.urlProperties = urlProperties;
        this.restClient = new CachingRestClient().setClientSubSystemCode(SERVICE_CODE);
        this.restClient.setWebCasUrl(urlProperties.url("cas.url"));
        this.restClient.setCasService(urlProperties.url("oppijanumerorekisteri-service.security-check"));
        this.restClient.setUseProxyAuthentication(true);
        
        this.serviceAccountClient = new CachingRestClient().setClientSubSystemCode(SERVICE_CODE);
        this.serviceAccountClient.setWebCasUrl(urlProperties.url("cas.url"));
        this.serviceAccountClient.setCasService(urlProperties.url("oppijanumerorekisteri-service.security-check"));
        this.serviceAccountClient.setUsername(serviceUsersProperties.getOppijanumerorekisteri().getUsername());
        this.serviceAccountClient.setPassword(serviceUsersProperties.getOppijanumerorekisteri().getPassword());
    }
    
    @Override
    public List<HenkiloPerustietoDto> getHenkilonPerustiedot(Collection<String> henkiloOid) {
        if (henkiloOid.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloPerustietosByHenkiloOidList");
        return retrying(FunctionalUtils.<List<HenkiloPerustietoDto>>io(
            () -> objectMapper.readerFor(new TypeReference<List<HenkiloPerustietoDto>>() {})
                    .readValue(IOUtils.toString(restClient.post(url, MediaType.APPLICATION_JSON,
                            objectMapper.writer().writeValueAsString(henkiloOid)).getEntity().getContent()))), 2).get()
                .orFail(mapper(url));
    }
    
    @Override
    public HenkilonYhteystiedotViewDto getHenkilonYhteystiedot(String henkiloOid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.yhteystiedot", henkiloOid);
        return retrying(FunctionalUtils.<HenkilonYhteystiedotViewDto>io(
                    () -> objectMapper.readerFor(HenkilonYhteystiedotViewDto.class)
                .readValue(restClient.getAsString(url))), 2).get()
                .orFail(mapper(url));
    }

    @Override
    public Set<String> getAllOidsForSamePerson(String personOid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.duplicateHenkilos");
        Map<String,Object> criteria = new HashMap<>();
        criteria.put("henkiloOids", singletonList(personOid));
        return Stream.concat(Stream.of(personOid),
            retrying(FunctionalUtils.<List<HenkiloViiteDto>>io(
                () ->  objectMapper.readerFor(new TypeReference<List<HenkiloViiteDto>>() {})
                    .readValue(IOUtils.toString(this.serviceAccountClient.post(url, MediaType.APPLICATION_JSON,
                        objectMapper.writeValueAsString(criteria)).getEntity().getContent()))), 2).get()
            .orFail(mapper(url)).stream().flatMap(viite -> Stream.of(viite.getHenkiloOid(), viite.getMasterOid()))
        ).collect(toSet());
    }
    
    @Getter @Setter
    public static class HenkiloViiteDto {
        private String henkiloOid;
        private String masterOid;
    }
}
