package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.report.AccessRightReportRow;
import fi.vm.sade.kayttooikeus.service.report.accessrights.AccessRightReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Report generation endpoints")
@RestController
@RequestMapping(ReportController.REQUEST_MAPPING)
@RequiredArgsConstructor
public class ReportController {

    protected static final String REQUEST_MAPPING = "/reports";
    protected static final String ACCESS_RIGHTS = "/accessrights";

    private final AccessRightReport accessRightReport;

    @GetMapping(ACCESS_RIGHTS + "/{oid}")
    @PreAuthorize("@permissionCheckerServiceImpl.checkRoleForOrganisation({#oid}, {'KAYTTOOIKEUS': {'ACCESS_RIGHTS_REPORT'}})")
    @Operation(summary = "Report of access rights for all users under given organisation")
    public List<AccessRightReportRow> getAccessRightsReport(@PathVariable(value = "oid") final String oid) {
        return accessRightReport.getForOrganisation(oid);
    }
}
