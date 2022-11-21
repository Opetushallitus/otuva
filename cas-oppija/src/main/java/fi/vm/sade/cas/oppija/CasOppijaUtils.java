package fi.vm.sade.cas.oppija;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CasOppijaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasOppijaUtils.class);

    private CasOppijaUtils() {
    }

    public static <T> Optional<T> resolveAttribute(Map<String, List<Object>> attributes, String attributeName, Class<T> type) {
        Object attribute = attributes.get(attributeName);
        if (attribute == null) {
            return Optional.empty();
        }
        if (type.isInstance(attribute)) {
            return Optional.of(type.cast(attribute));
        }
        if (attribute instanceof Iterable) {
            Iterable<T> iterable = (Iterable) attribute;
            for (Object value : iterable) {
                if (type.isInstance(value)) {
                    return Optional.of(type.cast(value));
                }
            }
        }
        LOGGER.warn("Cannot parse {} to {} (type={}, value={})", attributeName, type, attribute.getClass(), attribute);
        return Optional.empty();
    }

}
