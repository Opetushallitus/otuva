package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.kayttooikeus.Application;
import fi.vm.sade.kayttooikeus.util.HsqlDbServerTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AbstractTestExecutionListener;

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
        properties = {"mock.ldap=true", "spring.config.location=classpath:/kayttooikeus-test.yml",
                        "host-virkailija=localhost:9292",
                        "url-virkailija=http://localhost:9292"})
@TestExecutionListeners(inheritListeners = true, mergeMode = MergeMode.MERGE_WITH_DEFAULTS,
        listeners = {ApplicationTest.SetEnvTestExecutionListener.class})
@TestPropertySource("classpath:")
public @interface ApplicationTest {
    class SetEnvTestExecutionListener extends AbstractTestExecutionListener {
        @Override
        public void beforeTestClass(TestContext testContext) throws Exception {
            // OphProperties can not read yml, classpath resources nor understand the properties set in 
            // @SpringBootTest:
            System.setProperty("host-virkailija", "localhost:9292");
            System.setProperty("url-virkailija", "http://localhost:9292");
        }
    }
}
