package fi.vm.sade.kayttooikeus.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .build()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
      registry.addViewController("/swagger-ui/")
          .setViewName("forward:/swagger-ui/index.html");
    }

    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
      configurer.setUseTrailingSlashMatch(true);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        converter.setDefaultCharset(Charset.forName("UTF-8"));
        return converter;
    }

    @Override
    public void extendMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // Add this as primary converter. Spring tests which one to use in order of this list. This needs to be
        // before stringhttpmessageconverter or apis will return plain string as json content type string. (without
        // double quote marks)
        converters.add(0, mappingJackson2HttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(@NonNull ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false)
                .defaultContentType(MediaType.APPLICATION_JSON);
    }

}
