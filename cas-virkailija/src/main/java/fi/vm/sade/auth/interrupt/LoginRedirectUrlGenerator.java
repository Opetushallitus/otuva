package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.clients.KayttooikeusClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriClient;
import fi.vm.sade.properties.OphProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginRedirectUrlGenerator {
    private final KayttooikeusClient kayttooikeusClient;
    private final OppijanumerorekisteriClient oppijanumerorekisteriRestClient;
    private final OphProperties ophProperties;

    public String createRedirectUrl(String username, String urlProperty) {
        String oidHenkilo = this.kayttooikeusClient.getHenkiloOid(username);
        String loginToken = this.kayttooikeusClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return this.ophProperties.url(urlProperty, asiointiKieli, loginToken);
    }

    public String createRegistrationUrl(String username) {
        String oidHenkilo = this.kayttooikeusClient.getHenkiloOid(username);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return ophProperties.url("henkilo-ui.register", asiointiKieli);
    }
}
