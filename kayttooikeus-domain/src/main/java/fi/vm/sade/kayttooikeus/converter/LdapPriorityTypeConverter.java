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
        return attribute.getDbData();
    }

    @Override
    public LdapPriorityType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return LdapPriorityType.fromDbData(dbData);
    }

}
