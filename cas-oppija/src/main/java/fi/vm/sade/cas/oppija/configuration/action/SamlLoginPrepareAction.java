package fi.vm.sade.cas.oppija.configuration.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.VALTUUDET_ENABLED;

/**
 * Get possible valtuudet-parameter from url to enable/disable valtuudet login.
 */
public class SamlLoginPrepareAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlLoginPrepareAction.class);

    private Flow loginFlow;

    public SamlLoginPrepareAction(Flow loginFlow) {
        this.loginFlow = loginFlow;
    }

    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        LOGGER.info("VALTUUDET | Context contains valtuudet: {} | Valtuudet: {} | Context contains service: {} | Service: {}",
                context.getExternalContext().getRequestParameterMap().contains("valtuudet"),
                context.getExternalContext().getRequestParameterMap().get("valtuudet"),
                context.getExternalContext().getRequestParameterMap().contains("service"),
                context.getExternalContext().getRequestParameterMap().get("service"));
        if (context.getExternalContext().getRequestParameterMap().contains("valtuudet")) {
            LOGGER.info("SamlLoginPrepareAction: if -> put valtuudet to flow.");
            this.loginFlow.getAttributes().put("valtuudet", context.getExternalContext().getRequestParameterMap().getBoolean("valtuudet"));
        } else if (context.getExternalContext().getRequestParameterMap().contains("service")) {
            LOGGER.info("SamlLoginPrepareAction: else if -> put default valtuudet to flow: " + VALTUUDET_ENABLED);
            this.loginFlow.getAttributes().put("valtuudet", VALTUUDET_ENABLED);
        } else {
            LOGGER.info("SamlLoginPrepareAction: ELSE - DO NOTHING.");
        }
        LOGGER.info("SamlLoginPrepareAction returning VALTUUDET | Service: {} " +
                        "| Context contains valtuudet: {} " +
                        "| Context valtuudet: {} " +
                        "| loginFlow valtuudet: {} ",
                context.getExternalContext().getRequestParameterMap().get("service"),
                context.getExternalContext().getRequestParameterMap().contains("valtuudet"),
                context.getExternalContext().getRequestParameterMap().get("valtuudet"),
                this.loginFlow.getAttributes().get("valtuudet"));
        return success();
    }
}
