package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.exception.http.HttpAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

@Configuration
public class LogoutOnErrorDelegatedAuthenticationAction {
    // override default delegatedAuthenticationAction to automatically logout on error
    static final String TRANSITION_ID_LOGOUT = "logout";
    @Bean
    public Action delegatedAuthenticationAction(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager
    ) {
        return new DelegatedClientAuthenticationAction(context,delegatedClientAuthenticationWebflowManager ) {
            @Override
            public Event doExecute(RequestContext context) {
                try {
                    return super.doExecute(context);
                } catch (Exception e) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
            }

            @Override
            protected Event stopWebflow(Exception e, RequestContext requestContext) {
                if (e instanceof HttpAction) {
                    return handleLogout((HttpAction) e, RequestContextHolder.getRequestContext());
                }
                return super.stopWebflow(e, requestContext);
            }

            private Event handleLogout(HttpAction httpAction, RequestContext requestContext) {
                if (httpAction.getCode() == HttpConstants.TEMPORARY_REDIRECT) {
                    String redirectUrl = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext).getHeader(HttpConstants.LOCATION_HEADER);
                    WebUtils.putLogoutRedirectUrl(requestContext, redirectUrl);
                    return result(TRANSITION_ID_LOGOUT);
                }
                throw new IllegalArgumentException("Unhandled logout response code: " + httpAction.getCode());
            }
        };
    }
}
