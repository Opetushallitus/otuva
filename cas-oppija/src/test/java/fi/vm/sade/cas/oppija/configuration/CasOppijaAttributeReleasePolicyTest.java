package fi.vm.sade.cas.oppija.configuration;

import org.apereo.cas.services.RegisteredService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
class CasOppijaAttributeReleasePolicyTest {

    private final CasOppijaAttributeReleasePolicy policy =
            new CasOppijaAttributeReleasePolicy();

    @Test
    void shouldReleaseSuomenKansalaisuusTietokoodi() {
        Map<String, List<Object>> attributes = new HashMap<>();

        attributes.put(
                "SuomenKansalaisuusTietokoodi",
                List.of("TEST_VALUE")
        );
        attributes.put(
                "someAttributeThatShouldNotBeReleased",
                List.of("secret")
        );

        RegisteredService service = mock(RegisteredService.class);

        Map<String, List<Object>> result =
                policy.returnFinalAttributesCollection(attributes, service);

        assertThat(result)
                .containsEntry(
                        "SuomenKansalaisuusTietokoodi",
                        List.of("TEST_VALUE")
                )
                .doesNotContainKey("someAttributeThatShouldNotBeReleased");
    }
}
