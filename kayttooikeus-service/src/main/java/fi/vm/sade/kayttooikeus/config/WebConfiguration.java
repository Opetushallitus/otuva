package fi.vm.sade.kayttooikeus.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.ser.LocalDateSerializer;
import org.joda.time.LocalDate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.util.List;

@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/virkailija/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/kayttooikeus-ui-virkailija/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer matcher) {
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    private ObjectMapper objectMapper() {
        Jackson2ObjectMapperFactoryBean bean = new Jackson2ObjectMapperFactoryBean();
        bean.afterPropertiesSet();
        ObjectMapper objectMapper = bean.getObject();
        LocalDateSerializer localDateAsStringSerilizer = new LocalDateSerializer() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(_format.createFormatter(provider).print(value));
            }
        };
        JodaModule jodaModule = new JodaModule() {{
            // Need to be added here since won't effect if added after initialization:
            addSerializer(LocalDate.class, localDateAsStringSerilizer);
        }};
        objectMapper.registerModule(jodaModule);
        return objectMapper;
    }

    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }
    
    @Bean
    public ExposedResourceMessageBundleSource messageSource() {
        ExposedResourceMessageBundleSource source = new ExposedResourceMessageBundleSource();
        source.addBasenames("classpath:Messages");
        return source;
    }
}