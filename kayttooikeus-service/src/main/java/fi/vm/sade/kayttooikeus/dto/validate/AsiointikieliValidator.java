package fi.vm.sade.kayttooikeus.dto.validate;

import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class AsiointikieliValidator implements ConstraintValidator<ValidateAsiointikieli, KielisyysDto> {
    private static final Set<String> ALLOWED_KIELI_KOODIT = Set.of("fi", "sv", "en");

    @Override
    public boolean isValid(KielisyysDto value, ConstraintValidatorContext context) {
        return value != null
                && value.getKieliKoodi() != null
                && ALLOWED_KIELI_KOODIT.contains(value.getKieliKoodi());
    }
}
