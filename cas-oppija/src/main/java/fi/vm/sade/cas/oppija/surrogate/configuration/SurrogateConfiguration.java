package fi.vm.sade.cas.oppija.surrogate.configuration;

import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.cas.oppija.surrogate.SurrogateCredential;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationHandler;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationWebflowConfigurer;
import org.apereo.cas.authentication.*;
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
        plan.registerAuthenticationMetadataPopulator(new ImpersonatorAuthenticationMetaDataPopulator());
    }

    private static class ImpersonatorAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

        @Override
        public void populateAttributes(AuthenticationBuilder builder, AuthenticationTransaction transaction) {
            transaction.getPrimaryCredential()
                    .filter(SurrogateCredential.class::isInstance)
                    .map(SurrogateCredential.class::cast)
                    .ifPresent(credential -> credential.getAuthenticationAttributes().forEach(builder::mergeAttribute));
        }

        @Override
        public boolean supports(Credential credential) {
            return SurrogateCredential.class.isInstance(credential);
        }

    }

}
