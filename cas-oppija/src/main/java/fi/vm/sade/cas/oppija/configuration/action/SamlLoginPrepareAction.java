package fi.vm.sade.cas.oppija.configuration.action;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.VALTUUDET_ENABLED;

/**
 * Get possible valtuudet-parameter from url to enable/disable valtuudet login.
 */
public class SamlLoginPrepareAction extends AbstractAction {
    private Flow loginFlow;

    public SamlLoginPrepareAction(Flow loginFlow) {
        this.loginFlow = loginFlow;
    }

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        if (context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            this.loginFlow.getAttributes().put("valtuudet", context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet"));
        } else if (context.getExternalContext().getRequestParameterMap().contains("service")
                && context.getExternalContext().getRequestParameterMap().get("service").contains("initsession")) {
            this.loginFlow.getAttributes().put("valtuudet", VALTUUDET_ENABLED);
        }
        return success();
    }
}
