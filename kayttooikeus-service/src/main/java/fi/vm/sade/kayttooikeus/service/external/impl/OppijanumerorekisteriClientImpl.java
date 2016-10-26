package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.generic.rest.CachingRestClient;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.util.FunctionalUtils;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkiloPerustietoDto;
import fi.vm.sade.oppijanumerorekisteri.dto.HenkilonYhteystiedotViewDto;
import fi.vm.sade.properties.OphProperties;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

@Component
public class OppijanumerorekisteriClientImpl implements OppijanumerorekisteriClient {
    private final ObjectMapper objectMapper;
    private final OphProperties urlProperties;
    private final CachingRestClient restClient;

    @Autowired
    public OppijanumerorekisteriClientImpl(ObjectMapper objectMapper, OphProperties urlProperties) {
        this.objectMapper = objectMapper;
        this.urlProperties = urlProperties;
        this.restClient = new CachingRestClient().setClientSubSystemCode("kayttooikeus.kayttooikeuspalvelu-service");
        this.restClient.setWebCasUrl(urlProperties.url("cas.url"));
        this.restClient.setCasService(urlProperties.url("oppijanumerorekisteri-service.security-check"));
        this.restClient.setUseProxyAuthentication(true);
    }

    @Override
    public List<HenkiloPerustietoDto> getHenkilonPerustiedot(Collection<String> henkiloOid) {
        if (henkiloOid.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloPerustietosByHenkiloOidList");
        return retrying(FunctionalUtils.<List<HenkiloPerustietoDto>>io(
            () -> objectMapper.readerFor(new TypeReference<List<HenkiloPerustietoDto>>() {})
                    .readValue(IOUtils.toString(restClient.post(url, APPLICATION_JSON_UTF8.getType(),
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
}
