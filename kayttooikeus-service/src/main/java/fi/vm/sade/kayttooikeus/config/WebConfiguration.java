package fi.vm.sade.kayttooikeus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by autio on 23.9.2016.
 */
@Configuration
@EnableSwagger2
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/invite/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/kayttooikeus-ui-invite/");

        super.addResourceHandlers(registry);
    }
}
