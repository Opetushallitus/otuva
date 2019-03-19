package fi.vm.sade.cas.oppija.surrogate.token;

import fi.vm.sade.cas.oppija.surrogate.SurrogateTokenProvider;

import java.util.UUID;

public class UuidSurrogateTokenProvider implements SurrogateTokenProvider {

    @Override
    public String createToken() {
        return UUID.randomUUID().toString();
    }

}
