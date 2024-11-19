package fi.vm.sade.kayttooikeus.aspects;

import fi.vm.sade.auditlog.*;
import lombok.RequiredArgsConstructor;
import org.ietf.jgss.Oid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Optional;

import static fi.vm.sade.kayttooikeus.aspects.AuditHelper.*;

@Component
@RequiredArgsConstructor
public class AuditLogger {

    private final Audit audit;

    public void log(Operation operation, Target target, Changes changes) {
        audit.log(getUser(), operation, target, changes);
    }

    public static User getUser() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return getUser(((ServletRequestAttributes) requestAttributes).getRequest());
        }
        return new User(getIp(), null, null);
    }

    public static User getUser(HttpServletRequest request) {
        InetAddress ip = getIp(request);
        String userAgent = getUserAgent(request).orElse(null);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isOauth2User(authentication)) {
            return new Oauth2User(authentication.getName(), ip, userAgent);
        }

        Optional<Oid> oid = getOid(request);
        String session = getSession(request).orElse(null);

        if (oid.isPresent()) {
            return new User(oid.get(), ip, session, userAgent);
        } else {
            return new User(ip, session, userAgent);
        }
    }

    private static boolean isOauth2User(Authentication auth) {
        return auth != null && auth.getName() != null && !auth.getName().startsWith("1.");
    }
}
