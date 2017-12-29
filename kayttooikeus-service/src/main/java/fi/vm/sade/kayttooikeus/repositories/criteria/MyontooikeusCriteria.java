package fi.vm.sade.kayttooikeus.repositories.criteria;

import fi.vm.sade.kayttooikeus.service.impl.PermissionCheckerServiceImpl;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class MyontooikeusCriteria {

    private String palvelu;
    private String rooli;

    public static MyontooikeusCriteria oletus() {
        // käyttöoikeusanomuksien käsittely on sallittu vain ANOMUSTENHALLINTA_CRUD -käyttöoikeudella
        return MyontooikeusCriteria.builder()
                .palvelu(PermissionCheckerServiceImpl.PALVELU_ANOMUSTENHALLINTA)
                .rooli(PermissionCheckerServiceImpl.ROLE_CRUD)
                .build();
    }

}
