package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.config.properties.ServiceUsersProperties;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.FunctionalUtils;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkilonYhteystiedotViewDto;
import fi.vm.sade.properties.OphProperties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.io;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloDto;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Component
public class OppijanumerorekisteriClientImpl implements OppijanumerorekisteriClient {
    private static final String SERVICE_CODE = "kayttooikeus.kayttooikeuspalvelu-service";
    private final ObjectMapper objectMapper;
    private final OphProperties urlProperties;
    private final CachingRestClient proxyRestClient;
    private final CachingRestClient serviceAccountClient;

    @Autowired
    public OppijanumerorekisteriClientImpl(ObjectMapper objectMapper, OphProperties urlProperties,
                                           ServiceUsersProperties serviceUsersProperties) {
        this.objectMapper = objectMapper;
        this.urlProperties = urlProperties;
        this.proxyRestClient = new CachingRestClient().setClientSubSystemCode(SERVICE_CODE);
        this.proxyRestClient.setWebCasUrl(urlProperties.url("cas.url"));
        this.proxyRestClient.setCasService(urlProperties.url("oppijanumerorekisteri-service.security-check"));
        this.proxyRestClient.setUseProxyAuthentication(true);
        
        this.serviceAccountClient = new CachingRestClient().setClientSubSystemCode(SERVICE_CODE);
        this.serviceAccountClient.setWebCasUrl(urlProperties.url("cas.url"));
        this.serviceAccountClient.setCasService(urlProperties.url("oppijanumerorekisteri-service.security-check"));
        this.serviceAccountClient.setUsername(serviceUsersProperties.getOppijanumerorekisteri().getUsername());
        this.serviceAccountClient.setPassword(serviceUsersProperties.getOppijanumerorekisteri().getPassword());
    }

    @Override
    public HenkiloDto getHenkilo(String henkiloOid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo", henkiloOid);
        return io(() -> objectMapper.readValue(serviceAccountClient.get(url), HenkiloDto.class)).get();
    }

    @Override
    public List<HenkiloPerustietoDto> getHenkilonPerustiedot(Collection<String> henkiloOid) {
        if (henkiloOid.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloPerustietosByHenkiloOidList");
        return retrying(FunctionalUtils.<List<HenkiloPerustietoDto>>io(
            () -> objectMapper.readerFor(new TypeReference<List<HenkiloPerustietoDto>>() {})
                    .readValue(IOUtils.toString(serviceAccountClient.post(url, MediaType.APPLICATION_JSON,
                            objectMapper.writer().writeValueAsString(henkiloOid)).getEntity().getContent()))), 2).get()
                .orFail(mapper(url));
    }

    @Override
    public HenkilonYhteystiedotViewDto getHenkilonYhteystiedot(String henkiloOid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.yhteystiedot", henkiloOid);
        return retrying(FunctionalUtils.<HenkilonYhteystiedotViewDto>io(
                    () -> objectMapper.readerFor(HenkilonYhteystiedotViewDto.class)
                .readValue(serviceAccountClient.getAsString(url))), 2).get()
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

    @Override
    public String getOidByHetu(String hetu) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.oidByHetu", hetu);
        return retrying(FunctionalUtils.io(
                () -> IOUtils.toString(serviceAccountClient.get(url))), 2).get()
                .orFail((RuntimeException e) -> {
                    if (e.getCause() instanceof CachingRestClient.HttpException) {
                        if (((CachingRestClient.HttpException) e.getCause()).getStatusCode() == 404) {
                            throw new NotFoundException("could not find oid with hetu: " + hetu);
                        }
                    }
                    return new ExternalServiceException(url, e.getMessage(), e);
                });
    }

    @Override
    public HenkiloPerustietoDto getPerustietoByOid(String oid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.henkiloPerustieto");
        Map<String,Object> data = new HashMap<>();
        data.put("oidHenkilo", oid);

        return retrying(FunctionalUtils.<HenkiloPerustiedotDto>io(
                () -> objectMapper.readerFor(HenkiloPerustiedotDto.class)
                        .readValue(this.serviceAccountClient.post(url, MediaType.APPLICATION_JSON,
                                objectMapper.writeValueAsString(data)).getEntity().getContent())), 2).get()
                .orFail(mapper(url));
    }

    @Override
    public HenkilonYhteystiedotViewDto getYhteystiedotByOid(String oid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.yhteystiedotByOid", oid);
        return retrying(FunctionalUtils.<HenkilonYhteystiedotViewDto>io(
                () -> objectMapper.readerFor(HenkilonYhteystiedotViewDto.class)
                        .readValue(serviceAccountClient.getAsString(url))), 2).get()
                .orFail(mapper(url));
    }

    //ONR uses java.time.LocalDate
    public static class HenkiloPerustiedotDto extends HenkiloPerustietoDto {
        public void setSyntymaaika(String localDate) {
            if (!StringUtils.isEmpty(localDate)) {
                this.setSyntymaaika(LocalDate.parse(localDate));
            }
        }
    }

    @Getter @Setter
    public static class HenkiloViiteDto {
        private String henkiloOid;
        private String masterOid;
    }
}
