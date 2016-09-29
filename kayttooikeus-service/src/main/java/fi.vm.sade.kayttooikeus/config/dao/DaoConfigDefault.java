package fi.vm.sade.kayttooikeus.config.dao;

import fi.vm.sade.kayttooikeus.config.properties.DatasourceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import static fi.vm.sade.kayttooikeus.config.dao.DaoConfigurations.PropertiesBuilder.builder;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 15.05
 */
@Configuration
@Profile({"default", "dev"})
public class DaoConfigDefault {
    @Value("#{systemProperties['use.memory.database'] != null and systemProperties['use.memory.database']}")
    private boolean useInMemoryDb;

    @Autowired
    private Environment environment;

    @Autowired
    private DatasourceProperties datasourceProperties;

    @Bean
    public DaoConfigurations daoConfigurations() {
        return DaoConfigurations.builder()
                .useInMemoryDb(useInMemoryDb)
                .useFlyway(useInMemoryDb)
                .jpaProperties(builder()
                        .put("hibernate.show_sql", environment.getProperty("jpa.showSql", Boolean.class, false))
                        .put("hibernate.hbm2ddl.auto", environment.getProperty("jpa.schema-update"))
                    .build())
                .mainDataSourceProperties(builder()
                        .put("url", datasourceProperties.getUrl())
                        .put("user", datasourceProperties.getUser())
                        .put("password", datasourceProperties.getPassword())
                    .build())
                .memoryDataSourceProperties(builder()
                        .put("database", "jdbc:hsqldb:file:auth")
                        .put("user", "SA")
                    .build())
                .build();
    }
}
