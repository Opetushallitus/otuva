package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttajaTyyppi;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloCreateDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioHenkiloUpdateDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(value = "/organisaatiohenkilo", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "/organisaatiohenkilo", description = "Organisaatiohenkilön käsittelyyn liittyvät operaatiot.")
public class OrganisaatioHenkiloController {
    private OrganisaatioHenkiloService organisaatioHenkiloService;

    public OrganisaatioHenkiloController(OrganisaatioHenkiloService organisaatioHenkiloService) {
        this.organisaatioHenkiloService = organisaatioHenkiloService;
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ',"
            + "'ROLE_APP_KAYTTOOIKEUS_CRUD',"
            + "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @RequestMapping(value = "/current/availablehenkilotype", method = RequestMethod.GET)
    @Operation(summary = "Listaa sallitut organisaatiohenkilötyypit henkilöiden luontiin liittyen.",
            description = "Listaa ne organisaatiohenkilötyypit joita kirjautunt käyttäjä saa luoda henkilöhallintaan.")
    public List<KayttajaTyyppi> listPossibleHenkiloTypesByCurrentHenkilo() {
        return organisaatioHenkiloService.listPossibleHenkiloTypesAccessibleForCurrentUser();
    }

    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ'," +
            "'ROLE_APP_KAYTTOOIKEUS_CRUD'," +
            "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    @GetMapping("/organisaatioOid")
    @Operation(summary = "Listaa henkilöiden organisaatio OID:t annettujen hakukriteerien mukaisesti")
    public Collection<String> listOrganisaatioOidBy(OrganisaatioHenkiloCriteria criteria) {
        return organisaatioHenkiloService.listOrganisaatioOidBy(criteria);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(#organisaatioHenkiloList, {'KAYTTOOIKEUS': {'CRUD'}})")
    @PostMapping(value = "/{oid}/findOrCreate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganisaatioHenkiloDto> findOrCreateOrganisaatioHenkilos(@PathVariable(value = "oid") String oidHenkilo,
                                                                         @RequestBody List<OrganisaatioHenkiloCreateDto> organisaatioHenkiloList) {
        return this.organisaatioHenkiloService.addOrganisaatioHenkilot(oidHenkilo, organisaatioHenkiloList);
    }

    @PreAuthorize("@permissionCheckerServiceImpl.hasRoleForOrganisations(#organisaatioHenkiloList, {'KAYTTOOIKEUS': {'CRUD'}})")
    @PutMapping(value = "/{oid}/createOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganisaatioHenkiloDto> updateOrganisaatioHenkilos(@PathVariable(value = "oid") String oidHenkilo,
                                                                   @RequestBody List<OrganisaatioHenkiloUpdateDto> organisaatioHenkiloList) {
        return this.organisaatioHenkiloService.createOrUpdateOrganisaatioHenkilos(oidHenkilo, organisaatioHenkiloList);
    }

    @Operation(summary = "Passsivoi henkilön organisaation ja kaikki tähän liittyvät käyttöoikeudet.")
    @PreAuthorize("@permissionCheckerServiceImpl.checkRoleForOrganisation({#henkiloOrganisationOid}, {'KAYTTOOIKEUS': {'CRUD'}})")
    @RequestMapping(value = "/{oid}/{henkiloOrganisationOid}", method = RequestMethod.DELETE)
    public void passivoiHenkiloOrganisation(@PathVariable("oid") String oidHenkilo,
                                            @PathVariable("henkiloOrganisationOid") String henkiloOrganisationOid) {
        this.organisaatioHenkiloService.passivoiHenkiloOrganisation(oidHenkilo, henkiloOrganisationOid);
    }

}
