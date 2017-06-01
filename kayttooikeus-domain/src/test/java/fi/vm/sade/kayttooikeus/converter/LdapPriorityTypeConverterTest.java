package fi.vm.sade.kayttooikeus.converter;

import fi.vm.sade.kayttooikeus.model.LdapPriorityType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class LdapPriorityTypeConverterTest {

    private LdapPriorityTypeConverter converter;

    @Before
    public void setup() {
        converter = new LdapPriorityTypeConverter();
    }

    @Test
    public void convertToDatabaseColumnWithNullShouldReturnNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    public void convertToEntityAttributeWithNullShouldReturnNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    public void convertShouldWork() {
        for (LdapPriorityType original : LdapPriorityType.values()) {
            Integer dbData = converter.convertToDatabaseColumn(original);
            LdapPriorityType converted = converter.convertToEntityAttribute(dbData);
            assertThat(converted).isEqualTo(original);
        }
    }

}
