package fi.vm.sade.kayttooikeus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 15.24
 */
@Configuration
@PropertySource(value = {
        "classpath:/default-props.properties",
        "file:///${user.home:''}/oph-configuration/common.properties"
}, ignoreResourceNotFound = false)
public class PropertiesConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
