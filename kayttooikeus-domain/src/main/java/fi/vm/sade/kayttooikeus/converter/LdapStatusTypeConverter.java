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
        return attribute.getDbData();
    }

    @Override
    public LdapStatusType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return LdapStatusType.fromDbData(dbData);
    }

}
