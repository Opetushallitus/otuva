package fi.vm.sade.otuva.mocksubstanceservice;

import fi.vm.sade.otuva.mocksubstanceservice.properties.OtuvaMockSubstanceServiceProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OtuvaMockSubstanceServiceProperties.class)
public class Application {

	public static void main(String[] args) {
		Flyway flyway = Flyway.configure().dataSource("jdbc:postgresql://localhost:5440/otuvamocksubstanceservice", "oph", "oph").load();
		flyway.migrate();
		SpringApplication.run(Application.class, args);
	}

}
