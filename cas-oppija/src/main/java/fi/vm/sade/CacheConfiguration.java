package fi.vm.sade;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {
    public static final String CACHE_NAME_OAUTH2_BEARER = "oauth2Bearer";
}
