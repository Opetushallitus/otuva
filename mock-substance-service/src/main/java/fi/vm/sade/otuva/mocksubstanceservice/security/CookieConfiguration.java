package fi.vm.sade.otuva.mocksubstanceservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieConfiguration {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setSameSite(null);
        defaultCookieSerializer.setCookieName("JSESSIONID");
        defaultCookieSerializer.setUseSecureCookie(true);
        return defaultCookieSerializer;
    }
}
