package fi.vm.sade.cas.oppija.configuration.action;

import fi.vm.sade.cas.oppija.CasOppijaConstants;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.saml.exceptions.SAMLException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class DelegatedAuthenticationActionConfiguration {
    // override default delegatedAuthenticationAction to automatically logout on error
    @Bean
    public Action delegatedAuthenticationAction(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager,
            final DelegatedClientAuthenticationFailureEvaluator failureEvaluator
    ) {
        return new DelegatedClientAuthenticationAction(context, delegatedClientAuthenticationWebflowManager, failureEvaluator) {
            @Override
            public Event doExecute(RequestContext requestContext) {
                HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                if (isLogoutRequest(request)) {
                    return result(CasOppijaConstants.TRANSITION_ID_IDP_LOGOUT);
                }
                return super.doExecute(requestContext);
            }

            @Override
            protected Event stopWebflow(Exception e, RequestContext requestContext) {
                if (e instanceof SAMLException) {
                    return result(CasWebflowConstants.TRANSITION_ID_CANCEL);
                }
                return super.stopWebflow(e, requestContext);
            }
        };
    }
}
