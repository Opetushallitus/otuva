package fi.vm.sade.kayttooikeus.converter;

import fi.vm.sade.kayttooikeus.model.LdapStatusType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class LdapStatusTypeConverter implements AttributeConverter<LdapStatusType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LdapStatusType attribute) {
        if (attribute == null) {
            return null;
        }
        switch (attribute) {
            case IN_QUEUE:
                return 0;
            case RETRY:
                return 1;
            case FAILED:
                return 2;
            default:
                throw new IllegalArgumentException("Tuntematon LdapStatusType attribute: " + attribute);
        }
    }

    @Override
    public LdapStatusType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        switch (dbData) {
            case 0:
                return LdapStatusType.IN_QUEUE;
            case 1:
                return LdapStatusType.RETRY;
            case 2:
                return LdapStatusType.FAILED;
            default:
                throw new IllegalArgumentException("Tuntematon LdapStatusType dbData: " + dbData);
        }
    }

}
