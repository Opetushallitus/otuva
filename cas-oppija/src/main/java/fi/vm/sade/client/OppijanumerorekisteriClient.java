package fi.vm.sade.client;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OppijanumerorekisteriClient {
    private final Oauth2Client httpClient;
    private final Gson gson = new Gson();

    @Value("${oppijanumerorekisteri.baseurl}")
    private String oppijanumerorekisteriBaseurl;

    public Optional<String> getOidByHetu(String hetu) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(oppijanumerorekisteriBaseurl + "/s2s/oidByHetu/" + hetu))
                .GET();
            return Optional.of(httpClient.executeRequest(request).body());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public Optional<String> getOidByEidas(String eidas) {
        try {
            var request = HttpRequest.newBuilder()
                .uri(URI.create(oppijanumerorekisteriBaseurl + "/s2s/oidByEidas"))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(gson.toJson(new OidByEidasBody(eidas))));
            return Optional.of(httpClient.executeRequest(request).body());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    record OidByEidasBody(String eidasTunniste) {}
}
