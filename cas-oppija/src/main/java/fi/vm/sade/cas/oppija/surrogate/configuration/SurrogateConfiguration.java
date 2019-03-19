package fi.vm.sade.cas.oppija.surrogate.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.service.PersonService;
import fi.vm.sade.cas.oppija.surrogate.SurrogateProperties;
import fi.vm.sade.cas.oppija.surrogate.SurrogateService;
import fi.vm.sade.cas.oppija.surrogate.SurrogateSessionStorage;
import fi.vm.sade.cas.oppija.surrogate.SurrogateTokenProvider;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationHandler;
import fi.vm.sade.cas.oppija.surrogate.auth.SurrogateAuthenticationWebflowConfigurer;
import fi.vm.sade.cas.oppija.surrogate.service.SurrogateServiceImpl;
import fi.vm.sade.cas.oppija.surrogate.session.InMemorySurrogateSessionStorage;
import fi.vm.sade.cas.oppija.surrogate.session.SurrogateSessionCleaner;
import fi.vm.sade.cas.oppija.surrogate.token.UuidSurrogateTokenProvider;
import fi.vm.sade.javautils.httpclient.OphHttpClient;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class SurrogateConfiguration implements CasWebflowExecutionPlanConfigurer, AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    private SurrogateAuthenticationWebflowConfigurer surrogateAuthenticationWebflowConfigurer;
    @Autowired
    private PersonService personService;
    @Autowired
    private OphHttpClient httpClient;
    @Autowired
    private SurrogateProperties properties;
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public SurrogateService surrogateService() {
        return new SurrogateServiceImpl(httpClient, properties, objectMapper, surrogateSessionStorage(), surrogateTokenProvider());
    }

    @Bean
    public SurrogateSessionStorage surrogateSessionStorage() {
        return new InMemorySurrogateSessionStorage();
    }

    @Bean
    public SurrogateTokenProvider surrogateTokenProvider() {
        return new UuidSurrogateTokenProvider();
    }

    @Bean
    public SurrogateSessionCleaner surrogateSessionCleaner() {
        return new SurrogateSessionCleaner(surrogateSessionStorage(), properties);
    }

    @Override
    public void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(surrogateAuthenticationWebflowConfigurer);
    }

    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationHandler(new SurrogateAuthenticationHandler(surrogateService(), personService,
                PrincipalFactoryUtils.newPrincipalFactory()));
    }

    @Scheduled(fixedDelayString = "${valtuudet.session-timeout}", initialDelayString = "${valtuudet.session-timeout}")
    public void cleanSurrogateSession() {
        surrogateSessionCleaner().clean();
    }

}
