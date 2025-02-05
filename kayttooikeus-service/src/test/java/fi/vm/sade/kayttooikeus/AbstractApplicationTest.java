package fi.vm.sade.kayttooikeus;

import fi.vm.sade.kayttooikeus.config.ApplicationTest;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@ApplicationTest
@EnableWireMock
public abstract class AbstractApplicationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("hibernate.hbm2ddl.auto", "update");
    }

    /**
     * @see #resource(String)
     * Version annotated to return JSON
     */
    protected String jsonResource(String classpathResource) {
        return resource(classpathResource);
    }

    /**
     * Convenience method for loading classpath resources. In Spring's form for IDEA link/refactoring support.
     * @param resource in Spring's resource form, e.g. classpath:/some-file
     * @return resource contents as a string
     */
    protected String resource(String resource) {
        try {
            return new String(applicationContext.getResource(resource).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not load resource: " + resource + ", cause: " + e.getMessage(), e);
        }
    }
}
