package fi.vm.sade.kayttooikeus.config;

import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.filter.UrlHandlerFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {
    private final JsonMapper jsonMapper;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/swagger-ui")
          .setViewName("forward:/swagger-ui/index.html");
      registry.addViewController("/swagger-ui/")
          .setViewName("forward:/swagger-ui/index.html");
    }

    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        converter.setDefaultCharset(Charset.forName("UTF-8"));
        return converter;
    }

    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilterRegistration() {
        FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
            UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build()
        );
        registration.setOrder(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1);
        return registration;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Keep byte[] responses, such as Springdoc's /v3/api-docs, handled as raw bytes instead of Base64 JSON.
        // Jackson must still stay before StringHttpMessageConverter for existing JSON string response behavior.
        int jacksonIndex = 0;
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof ByteArrayHttpMessageConverter) {
                jacksonIndex = i + 1;
            }
        }
        converters.add(jacksonIndex, jacksonJsonHttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

}
