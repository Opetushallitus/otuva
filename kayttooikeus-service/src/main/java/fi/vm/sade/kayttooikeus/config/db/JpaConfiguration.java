package fi.vm.sade.kayttooikeus.config.db;

import com.zaxxer.hikari.HikariDataSource;
import fi.vm.sade.kayttooikeus.config.properties.HikariProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@EnableJpaRepositories(basePackages = {"fi.vm.sade.kayttooikeus.repositories"})
@EntityScan({"fi.vm.sade.kayttooikeus.model"})
@EnableTransactionManagement
public class JpaConfiguration {
    private HikariProperties hikariProperties;

    @Autowired
    public JpaConfiguration(HikariProperties hikariProperties) {
        this.hikariProperties = hikariProperties;
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        return new HikariDataSource(hikariProperties);
    }
}
