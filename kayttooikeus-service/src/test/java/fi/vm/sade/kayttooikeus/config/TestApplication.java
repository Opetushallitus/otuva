package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.kayttooikeus.Application;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.*;

/**
 * User: tommiratamaa
 * Date: 13/10/2016
 * Time: 15.12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@SpringBootTest(classes = Application.class,
        properties = {"mock.ldap=true", "spring.config.location=classpath:/kayttooikeus-test.yml"})
public @interface TestApplication {
}
