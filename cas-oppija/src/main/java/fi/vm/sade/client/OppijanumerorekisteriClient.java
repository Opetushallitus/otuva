package fi.vm.sade.client;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OppijanumerorekisteriClient {
    private final Oauth2Client httpClient;
    private final Gson gson = new Gson();

    @Value("oppijanumerorekisteri.baseurl")
    private String oppijanumerorekisteriBaseurl;

    public String getOidByHetu(String hetu) {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(oppijanumerorekisteriBaseurl + "/s2s/oidByHetu/" + hetu))
            .GET();
        return httpClient.executeRequest(request).body();
    }

    public String getOidByEidas(String eidas) {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(oppijanumerorekisteriBaseurl + "/s2s/oidByEidas"))
            .POST(BodyPublishers.ofString(gson.toJson(new OidByEidasBody(eidas))));
        return httpClient.executeRequest(request).body();
    }

    record OidByEidasBody(String eidasTunniste) {}
}
