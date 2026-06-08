package fi.vm.sade.kayttooikeus.dto.validate;

import fi.vm.sade.kayttooikeus.repositories.dto.HenkiloCreateByKutsuDto;
import fi.vm.sade.oppijanumerorekisteri.dto.KielisyysDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateAsiointikieliTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsSupportedKieliKoodit() {
        assertThat(validate("fi")).isEmpty();
        assertThat(validate("sv")).isEmpty();
        assertThat(validate("en")).isEmpty();
    }

    @Test
    void rejectsUnsupportedKieliKoodit() {
        assertThat(validate("FI")).isNotEmpty();
        assertThat(validate("de")).isNotEmpty();
        assertThat(validate(null)).isNotEmpty();
    }

    @Test
    void rejectsMissingAsiointiKieli() {
        HenkiloCreateByKutsuDto dto = new HenkiloCreateByKutsuDto("Matti", null, null, null);

        assertThat(validator.validate(dto)).isNotEmpty();
    }

    private Set<ConstraintViolation<HenkiloCreateByKutsuDto>> validate(String kieliKoodi) {
        HenkiloCreateByKutsuDto dto = new HenkiloCreateByKutsuDto("Matti", new KielisyysDto(kieliKoodi, null), null, null);
        return validator.validate(dto);
    }
}
