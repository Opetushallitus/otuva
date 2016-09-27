package fi.vm.sade.kayttooikeus.config;

import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Created by autio on 27.9.2016.
 */
@Configuration
@Profile("default")
public class DispatcherServletCustomConfiguration {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    @Bean
    public ServletRegistrationBean dispatcherServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(
                dispatcherServlet(), "/kayttooikeus-service/*");
        registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);
        return registration;
    }
}
