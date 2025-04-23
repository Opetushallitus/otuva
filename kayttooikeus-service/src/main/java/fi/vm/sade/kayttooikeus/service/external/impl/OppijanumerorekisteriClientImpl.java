package fi.vm.sade.kayttooikeus.service.external.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.sade.kayttooikeus.service.dto.HenkiloVahvaTunnistusDto;
import fi.vm.sade.kayttooikeus.service.dto.HenkiloYhteystiedotDto;
import fi.vm.sade.kayttooikeus.service.exception.NotFoundException;
import fi.vm.sade.kayttooikeus.service.external.ExternalServiceException;
import fi.vm.sade.kayttooikeus.service.external.OppijanumerorekisteriClient;
import fi.vm.sade.kayttooikeus.service.external.OtuvaOauth2Client;
import fi.vm.sade.kayttooikeus.service.impl.KayttoOikeusServiceImpl;
import fi.vm.sade.kayttooikeus.util.UserDetailsUtil;
import fi.vm.sade.oppijanumerorekisteri.dto.*;
import fi.vm.sade.properties.OphProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static fi.vm.sade.kayttooikeus.service.external.ExternalServiceException.mapper;
import static fi.vm.sade.kayttooikeus.service.external.impl.HttpClientUtil.noContentOrNotFoundException;
import static fi.vm.sade.kayttooikeus.util.FunctionalUtils.retrying;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

@Slf4j
@RequiredArgsConstructor
@Component
public class OppijanumerorekisteriClientImpl implements OppijanumerorekisteriClient {
    private final ObjectMapper objectMapper;
    private final OtuvaOauth2Client httpClient;
    private final OphProperties urlProperties;

    @Override
    public List<HenkiloPerustietoDto> getHenkilonPerustiedot(Collection<String> henkiloOid) {
        if (henkiloOid.isEmpty()) {
            return new ArrayList<>();
        }
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloPerustietosByHenkiloOidList");
        Supplier<List<HenkiloPerustietoDto>> action = () -> post(url, henkiloOid, HenkiloPerustietoDto[].class, 200)
                .map(array -> Arrays.stream(array).collect(toList()))
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public Set<String> getAllOidsForSamePerson(String personOid) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.duplicateHenkilos");
        Map<String,Object> criteria = new HashMap<>();
        criteria.put("henkiloOids", singletonList(personOid));
        Supplier<List<HenkiloViiteDto>> action = () -> post(url, criteria, HenkiloViiteDto[].class, 200)
                .map(array -> Arrays.stream(array).collect(toList()))
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return Stream.concat(Stream.of(personOid), retrying(action, 2).get().orFail(mapper(url))
                .stream().flatMap(viite -> Stream.of(viite.getHenkiloOid(), viite.getMasterOid()))).collect(toSet());
    }

    @Override
    public String getOidByHetu(String hetu) {
        String url = urlProperties.url("oppijanumerorekisteri-service.s2s.oidByHetu", hetu);
        Supplier<String> action = () -> get(url)
                .orElseThrow(() -> new NotFoundException("could not find oid with hetu: " + hetu));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public List<HenkiloHakuPerustietoDto> getAllByOids(long page, long limit, List<String> oidHenkiloList) {
        if (oidHenkiloList == null || oidHenkiloList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> params = new HashMap<String, String>() {{
            put("offset", Long.toString(page));
            put("limit", Long.toString(limit));
        }};
        Map<String, List<String>> data = new HashMap<>();
        data.put("henkiloOids", oidHenkiloList);

        String url = this.urlProperties.url("oppijanumerorekisteri-service.s2s.henkilohaku-list-as-admin", params);
        Supplier<List<HenkiloHakuPerustietoDto>> action = () -> post(url, data, HenkiloHakuPerustietoDto[].class, 200)
                .map(array -> Arrays.stream(array).collect(toList()))
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public List<String> getModifiedSince(LocalDateTime dateTime, long offset, long amount) {
        Map<String, String> params = new HashMap<String, String>() {{
            put("offset", Long.toString(offset));
            put("amount", Long.toString(amount));
        }};
        String url = this.urlProperties.url("oppijanumerorekisteri-service.s2s.modified-since", dateTime, params);
        Supplier<List<String>> action = () -> get(url, String[].class)
                .map(array -> Arrays.stream(array).collect(toList()))
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(e -> new ExternalServiceException(url, e.getMessage(), e));
    }

    @Override
    public HenkiloDto getHenkiloByOid(String oid) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloByOid", oid);

        Supplier<HenkiloDto> action = () -> get(url, HenkiloDto.class)
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public Map<String, HenkiloDto> getMasterHenkilosByOidList(List<String> oids) {
        if (oids.isEmpty()) { return Map.of(); }

        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo.masterHenkilosByOidList");
        Supplier<Map<String, HenkiloDto>> action = () -> {
            try {
                var req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(oids)));
                var response = httpClient.executeRequest(req);
                if (response.statusCode() == 200) {
                    TypeReference<Map<String, HenkiloDto>> typeRef = new TypeReference<>() {};
                    return objectMapper.readValue(response.body(), typeRef);
                } else {
                    throw noContentOrNotFoundException(url);
                }
            } catch  (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public Optional<HenkiloDto> findHenkiloByOid(String oid) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloByOid", oid);

        Supplier<Optional<HenkiloDto>> action = () -> get(url, HenkiloDto.class);
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public Optional<HenkiloDto> getHenkiloByHetu(String hetu) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo.henkiloByHetu", hetu);
        return get(url, HenkiloDto.class);
    }

    @Override
    public Collection<HenkiloYhteystiedotDto> listYhteystiedot(HenkiloHakuCriteria criteria) {
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.yhteystiedot");
        Supplier<Collection<HenkiloYhteystiedotDto>> action = () -> post(url, criteria, HenkiloYhteystiedotDto[].class, 200)
                .map(array -> Arrays.stream(array).collect(toList()))
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public String createHenkilo(HenkiloCreateDto henkiloCreateDto) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo");
        Supplier<String> action = () -> post(url, henkiloCreateDto, 201)
                .orElseThrow(() -> noContentOrNotFoundException(url));
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public void setStrongIdentifiedHetu(String oidHenkilo, HenkiloVahvaTunnistusDto henkiloVahvaTunnistusDto) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.cas.vahva-tunnistus", oidHenkilo);
        Supplier<String> action = () -> put(url, henkiloVahvaTunnistusDto);
        retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public void updateHenkilo(HenkiloUpdateDto henkiloUpdateDto) {
        String url = this.urlProperties.url("oppijanumerorekisteri-service.henkilo");
        Supplier<String> action = () -> put(url, henkiloUpdateDto);
        retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public void yhdistaHenkilot(String oid, Collection<String> duplicateOids) {
        String url = urlProperties.url("oppijanumerorekisteri-service.henkilo.byOid.yhdistaHenkilot", oid);
        Supplier<String> action = () -> post(url, duplicateOids, 200).get();
        retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public HenkiloOmattiedotDto getOmatTiedot(String oidHenkilo) {
        String url = this.urlProperties.url("oppijanumerorekisteri.henkilo.omattiedot-by-oid", oidHenkilo);
        Supplier<HenkiloOmattiedotDto> action = () -> get(url, HenkiloOmattiedotDto.class).get();
        return retrying(action, 2).get().orFail(mapper(url));
    }

    @Override
    public String resolveLanguageCodeForCurrentUser() {
        try {
            String currentUserOid = UserDetailsUtil.getCurrentUserOid();
            HenkiloDto currentUser = getHenkiloByOid(currentUserOid);
            String languageCode = UserDetailsUtil.getLanguageCode(currentUser);
            return languageCode.toUpperCase();
        } catch ( Exception e ) {
            log.error("Could not resolve preferred language for user, using \"{}\" as fallback", KayttoOikeusServiceImpl.FI, e);
            return KayttoOikeusServiceImpl.FI;
        }
    }

    private <T> Optional<T> get(String url, Class<T> type) {
        return get(url).map(str -> {
            try {
                return objectMapper.readValue(str, type);
            } catch  (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Optional<String> get(String url) {
        try {
            var request = HttpRequest.newBuilder().uri(new URI(url)).GET();
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return Optional.of(response.body());
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                throw noContentOrNotFoundException(url);
            }
        } catch  (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<T> post(String url, Object body, Class<T> type, int expectedStatus) {
        return post(url, body, expectedStatus).map(str -> {
            try {
                return objectMapper.readValue(str, type);
            } catch  (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Optional<String> post(String url, Object body, int expectedStatus) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == expectedStatus) {
                return Optional.of(response.body());
            } else if (response.statusCode() == 404) {
                return Optional.empty();
            } else {
                throw noContentOrNotFoundException(url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String put(String url, Object body) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            var response = httpClient.executeRequest(request);
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw noContentOrNotFoundException(url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Getter @Setter
    public static class HenkiloViiteDto {
        private String henkiloOid;
        private String masterOid;
    }

}
