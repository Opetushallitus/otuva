package fi.vm.sade.kayttooikeus.report;

import lombok.*;

import java.util.Date;

@Generated
@Getter
public class AccessRightReportRow {

    private final Long id;
    private final String personName;
    private final String personOid;
    private final String organisationOid;
    private final String accessRightName;
    private final Long accessRightId;
    private final Date startDate;
    private final Date endDate;
    private final Date modified;
    private final String modifiedBy;
    private String organisationName;

    @Builder(toBuilder = true, access = AccessLevel.PRIVATE)
    public AccessRightReportRow(
            Long id,
            String personName,
            String personOid,
            String organisationName,
            String organisationOid,
            String accessRightName,
            Long accessRightId,
            Date startDate,
            Date endDate,
            Date modified,
            String modifiedBy) {
        this.id = id;
        this.personName = personName;
        this.personOid = personOid;
        this.organisationName = organisationName;
        this.organisationOid = organisationOid;
        this.accessRightName = accessRightName;
        this.accessRightId = accessRightId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modified = modified;
        this.modifiedBy = modifiedBy;
    }

    public AccessRightReportRow withOrganisation(String name) {
        return toBuilder().organisationName(name).build();
    }
}
