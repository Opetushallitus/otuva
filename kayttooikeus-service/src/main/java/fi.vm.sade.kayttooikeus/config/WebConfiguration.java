package fi.vm.sade.kayttooikeus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

/**
 * Created by autio on 23.9.2016.
 */
@Configuration
@EnableSwagger2
public class WebConfiguration extends WebMvcConfigurerAdapter{

    @Bean
    public AuthenticationPrincipalArgumentResolver authenticationPrincipalArgumentResolver(){
        return new AuthenticationPrincipalArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(authenticationPrincipalArgumentResolver());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
