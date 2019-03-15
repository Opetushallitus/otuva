package fi.vm.sade.cas.oppija;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CasOppijaUtilsTest {

    @Test
    public void resolveAttributeWithEmptyAttributesShouldReturnEmpty() {
        Map<String, Object> attributes = Map.of();

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithCorrectTypeShouldReturnValue() {
        Map<String, Object> attributes = Map.of("key1", "value1", "key2", "value2");

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.of("value1"), attribute);
    }

    @Test
    public void resolveAttributeWithIncorrectTypeShouldReturnEmpty() {
        Map<String, Object> attributes = Map.of("key1", 1, "key2", 2);

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithEmptyListShouldReturnEmpty() {
        Map<String, Object> attributes = Map.of("key1", List.of());

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

    @Test
    public void resolveAttributeWithCorrectTypeInListShouldReturnValue() {
        Map<String, Object> attributes = Map.of("key1", List.of("value11", "value12"), "key2", List.of("value21", "value22"));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.of("value11"), attribute);
    }

    @Test
    public void resolveAttributeWithIncorrectTypeInListShouldReturnEmpty() {
        Map<String, Object> attributes = Map.of("key1", List.of(11, 12), "key2", List.of(21, 22));

        Optional<String> attribute = CasOppijaUtils.resolveAttribute(attributes, "key1", String.class);

        assertEquals(Optional.empty(), attribute);
    }

}
