package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.BooleanBuilder;
import fi.vm.sade.kayttooikeus.model.QKutsuOrganisaatio;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class AuthorizationCriteria {
    private final Boolean isAdmin;
    private final Boolean isMiniAdmin;
    private final String rootOrganisationOid;
    // Organisations user has HENKILONHALLINTA_CRUD and ANOMUSTENHALLINTA_CRUD
    private final Set<String> allowedOrganisaatioOids;

    public BooleanBuilder onConditionByAuthorization() {
        BooleanBuilder builder = new BooleanBuilder();
        if (this.isAdmin) {
            this.adminCondition(builder);
        }
        // Mini-admin is treated as regular user if he has not proper privileges to root organisation
        if (this.isMiniAdmin && allowedOrganisaatioOids.contains(this.rootOrganisationOid)) {
            this.miniAdminCondition(builder);
        }
        else if (!this.isAdmin) {
            this.normalUserCondition(builder);
        }

        return builder;
    }

    private void adminCondition(BooleanBuilder builder) {

    }

    private void miniAdminCondition(BooleanBuilder builder) {
        // Käyttöoikeus myöntöviite limit check
    }

    private void normalUserCondition(BooleanBuilder builder) {
        QKutsuOrganisaatio kutsuOrganisaatio = QKutsuOrganisaatio.kutsuOrganisaatio;
        // Has HENKILONHALLITA-/ANOMUSTENHALLINTA_CRUD to same or parent organisation
        builder.and(kutsuOrganisaatio.organisaatioOid.in(this.allowedOrganisaatioOids));

        // Organisaatioviite limit check
        // When granting to root organisation it has no organisaatioviite

        // Käyttöoikeus myöntöviite limit check

    }
}
