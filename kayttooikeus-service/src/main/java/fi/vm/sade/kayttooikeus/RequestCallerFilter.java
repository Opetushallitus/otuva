package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.config.security.OpintopolkuUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class RequestCallerFilter extends GenericFilterBean {
    public static final String CALLER_HENKILO_OID_ATTRIBUTE = RequestCallerFilter.class.getName() + ".callerHenkiloOid";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        getUserDetails(servletRequest).ifPresent(userDetails -> {
            servletRequest.setAttribute(CALLER_HENKILO_OID_ATTRIBUTE, userDetails.getOidHenkilo());
        });
        filterChain.doFilter(servletRequest, servletResponse);
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
