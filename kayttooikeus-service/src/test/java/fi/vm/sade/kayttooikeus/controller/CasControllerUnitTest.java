package fi.vm.sade.kayttooikeus.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CasControllerUnitTest {

    @InjectMocks
    private CasController controller;

    @Parameterized.Parameters(name = "shibbolethDecodeHeader: {0} -> {1}")
    public static Collection<Object[]> parameters() throws IOException {
        return asList(new Object[][] {
                {"Tero Testi", "Tero Testi"},
                {"Ã\u0084yrÃ¤mÃ¶", "Äyrämö"},
        });
    }

    @Parameterized.Parameter(0)
    public String encoded;
    @Parameterized.Parameter(1)
    public String decoded;

    @Before
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        assertThat(controller.decodeShibbolethHeader(encoded)).isEqualTo(decoded);
    }
}
