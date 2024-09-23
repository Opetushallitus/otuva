package fi.vm.sade.kayttooikeus.aspects;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Predicate;

public final class AuditHelper {

    private AuditHelper() {
    }

    public static Optional<Oid> getOid(HttpServletRequest request) {
        return Optional.ofNullable(request.getUserPrincipal()).map(Principal::getName).flatMap(AuditHelper::createOid);
    }

    private static Optional<Oid> createOid(String oid) {
        try {
            return Optional.of(new Oid(oid));
        } catch (GSSException e) {
            return Optional.empty();
        }
    }

    private static String getRemoteAddress(String xRealIp, String xForwardedFor, String remoteAddr, String requestURI) {
        Predicate<String> isNotBlank = (String txt) -> txt != null && !txt.isEmpty();
        if (isNotBlank.test(xRealIp)) {
            return xRealIp;
        }
        if (isNotBlank.test(xForwardedFor)) {
            return xForwardedFor;
        }
        return remoteAddr;
    }

    private static String getRemoteAddress(HttpServletRequest httpServletRequest) {
        return getRemoteAddress(httpServletRequest.getHeader("X-Real-IP"),
            httpServletRequest.getHeader("X-Forwarded-For"),
            httpServletRequest.getRemoteAddr(),
            httpServletRequest.getRequestURI());
    }

    public static InetAddress getIp(HttpServletRequest request) {
        try {
            return InetAddress.getByName(getRemoteAddress(request));
        } catch (UnknownHostException e) {
            return getIp();
        }
    }

    public static InetAddress getIp() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return InetAddress.getLoopbackAddress();
        }
    }

    public static Optional<String> getSession(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false)).map(HttpSession::getId);
    }

    public static Optional<String> getUserAgent(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

}
