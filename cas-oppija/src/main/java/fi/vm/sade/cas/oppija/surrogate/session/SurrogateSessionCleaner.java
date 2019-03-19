package fi.vm.sade.cas.oppija.surrogate.session;

import fi.vm.sade.cas.oppija.surrogate.SurrogateProperties;
import fi.vm.sade.cas.oppija.surrogate.SurrogateSessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SurrogateSessionCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateSessionCleaner.class);

    private final SurrogateSessionStorage sessionStorage;
    private final SurrogateProperties properties;

    public SurrogateSessionCleaner(SurrogateSessionStorage sessionStorage, SurrogateProperties properties) {
        this.sessionStorage = sessionStorage;
        this.properties = properties;
    }

    public void clean() {
        Instant instant = Instant.now().minus(properties.getSessionTimeout());
        long count = sessionStorage.clean(instant);
        LOGGER.info("[{}] expired surrogate sessions removed.", count);
    }

}
