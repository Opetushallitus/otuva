package fi.vm.sade.kayttooikeus.report;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class AccessRightReportRowTest {

    private static final String EMPTY = "";
    private static final String NAME = "Name of the organisation";

    @Test
    void withOrganisation() {
        AccessRightReportRow original = new AccessRightReportRow(
                BigInteger.ONE,
                "prsonName",
                "personOdi",
                EMPTY,
                "organisationOid",
                "accessRightsName",
                BigInteger.TEN,
                new Date(),
                new Date(),
                new Date(),
                "modifiedBy"
        );

        AccessRightReportRow withOrganisation = original.withOrganisation(NAME);

        assertThat(withOrganisation)
                .usingRecursiveComparison()
                .ignoringFields("organisationName")
                .isEqualTo(original);
        assertThat(original.getOrganisationName()).isEqualTo(EMPTY);
        assertThat(withOrganisation.getOrganisationName()).isEqualTo(NAME);
    }
}
