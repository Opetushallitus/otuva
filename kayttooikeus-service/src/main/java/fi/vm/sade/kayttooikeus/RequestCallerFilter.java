package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.config.security.OpintopolkuUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
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
            getUserDetails(servletRequest).ifPresent(userDetails -> {
                MDC.put(CALLER_HENKILO_OID_ATTRIBUTE, userDetails.getOidHenkilo());
                servletRequest.setAttribute(CALLER_HENKILO_OID_ATTRIBUTE, userDetails.getOidHenkilo());
            });
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(CALLER_HENKILO_OID_ATTRIBUTE);
        }
    }

    private Optional<OpintopolkuUserDetailsService.OpintopolkuUserDetailsl> getUserDetails(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest request) {
            var principal = request.getUserPrincipal();
            if (principal instanceof CasAuthenticationToken token) {
                var userDetails = token.getUserDetails();
                if (userDetails instanceof OpintopolkuUserDetailsService.OpintopolkuUserDetailsl casUserDetails) {
                    return Optional.of(casUserDetails);
                } else {
                    if (userDetails != null) {
                        log.info("Unknown UserDetails: {}", userDetails);
                    }
                }
            } else {
                if (principal != null) {
                    log.info("Unknown Principal: {}", principal);
                }
            }
        }
        return Optional.empty();
    }
}
