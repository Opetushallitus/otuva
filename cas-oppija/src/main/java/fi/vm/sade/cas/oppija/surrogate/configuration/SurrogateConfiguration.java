package fi.vm.sade.cas.oppija.surrogate.configuration;

import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationHandler;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SurrogateConfiguration implements CasWebflowExecutionPlanConfigurer, AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    private SurrogateAuthenticationWebflowConfigurer surrogateAuthenticationWebflowConfigurer;
    @Autowired
    private SurrogateService surrogateService;
    @Autowired
    private PersonService personService;

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(surrogateAuthenticationWebflowConfigurer);
    }

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new SurrogateAuthenticationHandler(surrogateService, personService,
                PrincipalFactoryUtils.newPrincipalFactory()));
    }

}
