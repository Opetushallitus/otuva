package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class StoreServiceParamAction extends AbstractServiceParamAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreServiceParamAction.class);
    private final CasConfigurationProperties casProperties;

    public StoreServiceParamAction(CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected Event doExecute(RequestContext requestContext) {
        var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        var service = request.getParameter(casProperties.getLogout().getRedirectParameter());
        if (service != null) {
            setServiceRedirectCookie(response, service);
            LOGGER.debug("Set service redirect cookie to value: " + service);
        }
        return null;
    }

}
