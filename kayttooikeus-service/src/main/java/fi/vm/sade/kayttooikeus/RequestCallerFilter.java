package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.config.security.OpintopolkuUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class RequestCallerFilter extends GenericFilterBean {
    public static final String CALLER_HENKILO_OID_ATTRIBUTE = RequestCallerFilter.class.getName() + ".callerHenkiloOid";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            var callerOid = getJwtToken(servletRequest).map(token -> token.getToken().getSubject())
                    .or(() -> getUserDetails(servletRequest).map(userDetails -> userDetails.getOidHenkilo()));
            callerOid.ifPresent(oid -> {
                MDC.put(CALLER_HENKILO_OID_ATTRIBUTE, oid);
                servletRequest.setAttribute(CALLER_HENKILO_OID_ATTRIBUTE, oid);
            });
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(CALLER_HENKILO_OID_ATTRIBUTE);
        }
    }

    private Optional<JwtAuthenticationToken> getJwtToken(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest request) {
            if (request.getUserPrincipal() instanceof JwtAuthenticationToken token) {
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    private Optional<OpintopolkuUserDetailsService.OpintopolkuUserDetailsl> getUserDetails(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest request) {
            if (request.getUserPrincipal() instanceof CasAuthenticationToken token) {
                if (token.getUserDetails() instanceof OpintopolkuUserDetailsService.OpintopolkuUserDetailsl casUserDetails) {
                    return Optional.of(casUserDetails);
                }
            }
        }
        return Optional.empty();
    }
}
