package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(value = "/organisaatiohenkilo", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "/organisaatiohenkilo", description = "Organisaatiohenkilön käsittelyyn liittyvät operaatiot.")
public class OrganisaatioHenkiloController {
    private OrganisaatioHenkiloService organisaatioHenkiloService;

    public OrganisaatioHenkiloController(OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.organisaatioHenkiloService = organisaatioHenkiloService;
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ'," +
            "'ROLE_APP_KAYTTOOIKEUS_CRUD'," +
            "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @GetMapping("/organisaatioOid")
    @Operation(summary = "Listaa henkilöiden organisaatio OID:t annettujen hakukriteerien mukaisesti")
    public Collection<String> listOrganisaatioOidBy(OrganisaatioHenkiloCriteria criteria) {
        return organisaatioHenkiloService.listOrganisaatioOidBy(criteria);
    }

    @Operation(summary = "Passsivoi henkilön organisaation ja kaikki tähän liittyvät käyttöoikeudet.")
    @PreAuthorize("@permissionCheckerServiceImpl.checkRoleForOrganisation({#henkiloOrganisationOid}, {'KAYTTOOIKEUS': {'CRUD'}})")
    @RequestMapping(value = "/{oid}/{henkiloOrganisationOid}", method = RequestMethod.DELETE)
    public void passivoiHenkiloOrganisation(@PathVariable("oid") String oidHenkilo,
                                            @PathVariable("henkiloOrganisationOid") String henkiloOrganisationOid) {
        this.organisaatioHenkiloService.passivoiHenkiloOrganisation(oidHenkilo, henkiloOrganisationOid);
    }

}
