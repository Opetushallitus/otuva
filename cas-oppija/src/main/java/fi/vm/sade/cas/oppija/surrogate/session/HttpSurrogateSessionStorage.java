package fi.vm.sade.cas.oppija.surrogate.session;

import fi.vm.sade.cas.oppija.surrogate.SurrogateSession;
import fi.vm.sade.cas.oppija.surrogate.SurrogateSessionStorage;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.time.Instant;

public class HttpSurrogateSessionStorage implements SurrogateSessionStorage {

    private final static String ATTRIBUTE_NAME = "surrogate";

    private HttpSession getHttpSession() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession(true);
    }

    @Override
    public void add(String token, SurrogateSession session) {
        getHttpSession().setAttribute(ATTRIBUTE_NAME, session);
    }

    @Override
    public SurrogateSession remove(String token) {
        HttpSession httpSession = getHttpSession();
        Object attribute = httpSession.getAttribute(ATTRIBUTE_NAME);
        if (attribute instanceof SurrogateSession) {
            httpSession.removeAttribute(ATTRIBUTE_NAME);
            return (SurrogateSession) attribute;
        }
        return null;
    }

    @Override
    public long clean(Instant instant) {
        // http sessions are automatically cleaned
        return 0;
    }

}
