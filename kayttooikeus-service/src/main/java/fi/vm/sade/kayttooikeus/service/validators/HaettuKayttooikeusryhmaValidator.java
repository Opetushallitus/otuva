package fi.vm.sade.kayttooikeus.service.validators;

import com.google.common.collect.Lists;
import fi.vm.sade.kayttooikeus.model.AnomuksenTila;
import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.service.PermissionCheckerService;
import fi.vm.sade.kayttooikeus.service.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class HaettuKayttooikeusryhmaValidator implements Validator {
    private PermissionCheckerService permissionCheckerService;

    @Autowired
    public HaettuKayttooikeusryhmaValidator(PermissionCheckerService permissionCheckerService) {
        this.permissionCheckerService = permissionCheckerService;
    }

    public boolean supports(Class clazz) {
        return HaettuKayttoOikeusRyhma.class.equals(clazz);
    }

    public void validate(Object object, Errors errors) {
        HaettuKayttoOikeusRyhma haettuKayttooikeusryhma = (HaettuKayttoOikeusRyhma) object;

        if (haettuKayttooikeusryhma.getAnomus().getAnomuksenTila() != AnomuksenTila.ANOTTU) {
            errors.reject("Anomus already handled");
        }

    }

}
