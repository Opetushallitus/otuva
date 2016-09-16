package fi.vm.sade.kayttooikeus.config.dao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

import static fi.vm.sade.kayttooikeus.config.dao.DaoConfigurations.PropertiesBuilder.builder;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 15.05
 */
@Configuration
@Profile({"test", "itest"})
public class DaoConfigTest {
    @Bean
    public DaoConfigurations daoConfigurations() {
        return DaoConfigurations.builder()
                .useInMemoryDb(true)
                .useFlyway(false)
                .jpaProperties(new Properties())
                .memoryDataSourceProperties(builder()
                        .put("database", "jdbc:hsqldb:mem:auth")
                        .build())
                .build();
    }
}
