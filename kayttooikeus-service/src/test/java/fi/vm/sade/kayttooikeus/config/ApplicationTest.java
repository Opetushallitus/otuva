package fi.vm.sade.kayttooikeus.config;

import fi.vm.sade.kayttooikeus.Application;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestExecutionListeners.MergeMode;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@SpringBootTest(classes = Application.class)
@TestExecutionListeners(mergeMode = MergeMode.MERGE_WITH_DEFAULTS)
public @interface ApplicationTest {
}
