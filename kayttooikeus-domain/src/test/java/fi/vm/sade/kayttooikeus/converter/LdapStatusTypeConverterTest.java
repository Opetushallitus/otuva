package fi.vm.sade.kayttooikeus.converter;

import fi.vm.sade.kayttooikeus.model.LdapStatusType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class LdapStatusTypeConverterTest {

    private LdapStatusTypeConverter converter;

    @Before
    public void setup() {
        converter = new LdapStatusTypeConverter();
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
        for (LdapStatusType original : LdapStatusType.values()) {
            Integer dbData = converter.convertToDatabaseColumn(original);
            LdapStatusType converted = converter.convertToEntityAttribute(dbData);
            assertThat(converted).isEqualTo(original);
        }
    }

}
