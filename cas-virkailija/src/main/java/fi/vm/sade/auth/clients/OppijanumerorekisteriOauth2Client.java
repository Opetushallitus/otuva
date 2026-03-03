package fi.vm.sade.auth.clients;

import java.net.URI;
import java.net.http.HttpRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import fi.vm.sade.auth.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oauth2.enabled", havingValue = "true")
public class OppijanumerorekisteriOauth2Client implements OppijanumerorekisteriClient {
    private final Oauth2Client httpClient;
    @Value("${oppijanumerorekisteri-service.baseurl}")
    private String baseurl;

    @Override
    public String getAsiointikieli(String henkiloOid) {
        var path = "henkilo/" + henkiloOid + "/asiointiKieli";
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseurl + path))
            .GET();
        return Json.parse(httpClient.executeRequest(request).body(), String.class);
    }

}
