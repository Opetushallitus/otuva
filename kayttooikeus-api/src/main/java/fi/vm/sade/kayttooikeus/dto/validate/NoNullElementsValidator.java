package fi.vm.sade.kayttooikeus.dto.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class NoNullElementsValidator implements ConstraintValidator<NoNullElements, Collection<?>> {
    public boolean isValid(Collection<?> collection, ConstraintValidatorContext context) {
        if (collection == null) return false;
        return !collection.contains(null);
    }
}
