package fi.vm.sade.kayttooikeus.converter;

import fi.vm.sade.kayttooikeus.model.LdapPriorityType;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class LdapPriorityTypeConverter implements AttributeConverter<LdapPriorityType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LdapPriorityType attribute) {
        if (attribute == null) {
            return null;
        }
        switch (attribute) {
            case BATCH:
                return 0;
            case ASAP:
                return 1;
            case NORMAL:
                return 2;
            case NIGHT:
                return 3;
            default:
                throw new IllegalArgumentException("Tuntematon LdapPriorityType attribute: " + attribute);
        }
    }

    @Override
    public LdapPriorityType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        switch (dbData) {
            case 0:
                return LdapPriorityType.BATCH;
            case 1:
                return LdapPriorityType.ASAP;
            case 2:
                return LdapPriorityType.NORMAL;
            case 3:
                return LdapPriorityType.NIGHT;
            default:
                throw new IllegalArgumentException("Tuntematon LdapPriorityType dbData: " + dbData);
        }
    }

}
