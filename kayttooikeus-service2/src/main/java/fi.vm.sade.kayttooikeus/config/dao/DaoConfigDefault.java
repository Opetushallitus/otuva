package fi.vm.sade.kayttooikeus.config.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static fi.vm.sade.kayttooikeus.config.dao.DaoConfigurations.PropertiesBuilder.builder;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 15.05
 */
@Configuration
@Profile("default")
public class DaoConfigDefault {
    @Value("#{systemProperties['use.memory.database'] != null and systemProperties['use.memory.database']}")
    private boolean useInMemoryDb;
    @Value("${authentication-service.postgresql.url}")
    private String mainDbUrl;
    @Value("${authentication-service.postgresql.user}")
    private String mainDbUser;
    @Value("${authentication-service.postgresql.password}")
    private String mainDbPassword;
    @Value("${jpa.showSql:false}")
    private boolean showSql;
    @Value("${jpa.schemaUpdate:''}")
    private String schemaUpdate;
    
    @Bean
    public DaoConfigurations daoConfigurations() {
        return DaoConfigurations.builder()
                .useInMemoryDb(useInMemoryDb)
                .useFlyway(useInMemoryDb)
                .jpaProperties(builder()
                        .put("hibernate.show_sql", showSql)
                        .put("hibernate.hbm2ddl.auto", schemaUpdate)
                    .build())
                .mainDataSourceProperties(builder()
                        .put("url", mainDbUrl)
                        .put("user", mainDbUser)
                        .put("password", mainDbPassword)
                    .build())
                .memoryDataSourceProperties(builder()
                        .put("database", "jdbc:hsqldb:file:auth")
                        .put("user", "SA")
                    .build())
                .build();
    }
}
