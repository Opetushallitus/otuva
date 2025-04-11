package fi.vm.sade.auth.discovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
@RequiredArgsConstructor
public class SamlDiscoveryFinalizerAction  extends BaseCasWebflowAction {

    protected final CasConfigurationProperties casProperties;
    protected final DelegatedClientAuthenticationConfigurationContext configContext;


    /**
     * @param requestContext
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecuteInternal(RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
                .orElseGet(() -> (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));

        String selectedIdP = request.getParameter("entityID");
        LOGGER.debug("Saving [{}] as entityID from discovery service to conversation scope", selectedIdP);
        if(selectedIdP != null) {
            requestContext.getConversationScope().put(
                    SamlDiscoveryWebflowConstants.CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP,
                    new SamlDiscoverySelectedIdP(selectedIdP, clientName)
            );
        }
        return this.success();
    }
}
