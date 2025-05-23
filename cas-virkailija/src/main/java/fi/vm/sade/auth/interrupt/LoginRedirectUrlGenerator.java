package fi.vm.sade.auth.interrupt;

import fi.vm.sade.auth.clients.KayttooikeusRestClient;
import fi.vm.sade.auth.clients.OppijanumerorekisteriRestClient;
import fi.vm.sade.properties.OphProperties;

import org.springframework.stereotype.Component;

@Component
public class LoginRedirectUrlGenerator {
    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient;
    private final OphProperties ophProperties;

    public LoginRedirectUrlGenerator(KayttooikeusRestClient kayttooikeusRestClient,
                               OppijanumerorekisteriRestClient oppijanumerorekisteriRestClient,
                               OphProperties ophProperties) {
        this.kayttooikeusRestClient = kayttooikeusRestClient;
        this.oppijanumerorekisteriRestClient = oppijanumerorekisteriRestClient;
        this.ophProperties = ophProperties;
    }

    public String createRedirectUrl(String username, String urlProperty) {
        String oidHenkilo = this.kayttooikeusRestClient.getHenkiloOid(username);
        String loginToken = this.kayttooikeusRestClient.createLoginToken(oidHenkilo);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return this.ophProperties.url(urlProperty, asiointiKieli, loginToken);
    }

    public String createRegistrationUrl(String username) {
        String oidHenkilo = this.kayttooikeusRestClient.getHenkiloOid(username);
        String asiointiKieli = this.oppijanumerorekisteriRestClient.getAsiointikieli(oidHenkilo);
        return ophProperties.url("henkilo-ui.register", asiointiKieli);
    }
}
