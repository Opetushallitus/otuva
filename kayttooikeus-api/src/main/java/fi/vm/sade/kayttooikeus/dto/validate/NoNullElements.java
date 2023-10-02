package fi.vm.sade.kayttooikeus.dto.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = NoNullElementsValidator.class)
@Documented
public @interface NoNullElements {
    String message() default "{fi.vm.sade.kayttooikeus.containsNulls}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
