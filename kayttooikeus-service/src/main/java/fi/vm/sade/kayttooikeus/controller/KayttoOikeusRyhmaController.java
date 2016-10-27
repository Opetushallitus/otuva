package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.service.KayttoOikeusService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaModifyDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;
import fi.vm.sade.kayttooikeus.service.dto.PalveluRoooliDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@RestController
@RequestMapping("/kayttooikeusryhma")
@Api(value = "/kayttooikeusryhma", description = "Käyttöoikeusryhmien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusRyhmaController {
    private KayttoOikeusService kayttoOikeusService;

    private AccessRightAuditLogger accessRightAuditLogger;

    private static final Logger logger = LoggerFactory.getLogger(KayttoOikeusRyhmaController.class);

    @Autowired
    public KayttoOikeusRyhmaController(KayttoOikeusService kayttoOikeusService,
                                       AccessRightAuditLogger accessRightAuditLogger) {
        this.kayttoOikeusService = kayttoOikeusService;
        this.accessRightAuditLogger = accessRightAuditLogger;
    }

    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Listaa kaikki käyttöoikeusryhmät.",
            notes = "Listaa kaikki käyttöoikeusryhmät, jotka on tallennettu henkilöhallintaan.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhma() {
        return kayttoOikeusService.listAllKayttoOikeusRyhmas();
    }

    @RequestMapping(value = "/organisaatio/{organisaatioOid}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Listaa käyttöoikeusryhmät organisaation mukaan.",
            notes = "Listaa käyttöoikeusryhmät, jotka ovat mahdollisia pyynnössä annetulle organisaatiolle.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhmasByOrdOid(@PathVariable("organisaatioOid") String organisaatioOid) {
        return kayttoOikeusService.listPossibleRyhmasByOrganization(organisaatioOid);
    }

    @RequestMapping(value = "/{oid}/{organisaatioOid}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee henkilön voimassa olevat käyttöoikeusryhmät.",
            notes = "Listaa kaikki annetun henkilön ja tämän annettuun organisaatioon "
                    + "liittyvät voimassaolevat sekä mahdollisesti myönnettävissä olevat "
                    + "käyttöoikeusryhmät DTO:n avulla.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmasIncludingHenkilos(
            @PathVariable("oid") String oid, @PathVariable("organisaatioOid") String organisaatioOid) {
        try {
            return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(
                    oid, organisaatioOid, getCurrentUserOid());
        } catch (Exception e) {
            logger.error("Error getting access right groups", e);
            return null;
        }
    }

    @RequestMapping(value = "/henkilo/{oid}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee henkilön käyttöoikeusryhmät.",
            notes = "Listaa henkilön kaikki käyttöoikeusryhmät sekä rajaa ne "
                    + "tiettyyn organisaatioon, jos kutsussa on annettu organisaatiorajoite.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmaByHenkilo(
            @PathVariable("oid") String oid,
            @QueryParam("ooid") String organisaatioOid) {
        return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(oid, organisaatioOid);
    }

    @RequestMapping(value = "/henkilo/current", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_HENKILONHALLINTA_READ',"
            + "'ROLE_APP_HENKILONHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_HENKILONHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee kirjautuneen henkilön käyttöoikeusryhmät.",
            notes = "Listaa nykyisen kirjautuneen henkilön kaikki käyttöoikeusryhmät "
                    + "sekä rajaa ne tiettyyn organisaatioon, jos kutsussa on "
                    + "annettu organisaatiorajoite.")
    public List<MyonnettyKayttoOikeusDto> listKayttoOikeusRyhmaForCurrentUser(@QueryParam("ooid") String organisaatioOid) {
        return kayttoOikeusService.listMyonnettyKayttoOikeusRyhmasByHenkiloAndOrganisaatio(getCurrentUserOid(), organisaatioOid);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_KOOSTEROOLIENHALLINTA_READ',"
            + "'ROLE_APP_KOOSTEROOLIENHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_KOOSTEROOLIENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee käyttöoikeusryhmän tiedot.",
            notes = "Hakee yhden käyttöoikeusryhmän kaikki tiedot "
                    + "annetun käyttöoikeusryhmän ID:n avulla.")
    public KayttoOikeusRyhmaDto getKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findKayttoOikeusRyhma(id);
    }


    @RequestMapping(value = "/{id}/sallitut", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Hakee käyttöoikeusryhmän rajoiteryhmät.",
            notes = "Listaa kaikki käyttöoikeusryhmälle alistetut käyttöoikeusryhmät "
                    + "eli ne ryhmät jotka tämän ryhmän myöntäminen mahdollistaa.")
    public List<KayttoOikeusRyhmaDto> getSubRyhmasByKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findSubRyhmasByMasterRyhma(id);
    }

    @RequestMapping(value = "/{id}/kayttooikeus", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_APP_KOOSTEROOLIENHALLINTA_READ',"
            + "'ROLE_APP_KOOSTEROOLIENHALLINTA_READ_UPDATE',"
            + "'ROLE_APP_KOOSTEROOLIENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Hakee käyttöoikeusryhmään kuuluvat palvelut ja roolit.",
            notes = "Listaa kaikki annettuun käyttöoikeusryhmään kuuluvat "
                    + "palvelut ja roolit yhdistelmäpareina DTO:n avulla.")
    public List<PalveluRoooliDto> getKayttoOikeusByKayttoOikeusRyhma(@PathVariable("id") Long id) {
        return kayttoOikeusService.findPalveluRoolisByKayttoOikeusRyhma(id);
    }


    @RequestMapping(method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ROLE_APP_KOOSTEROOLIENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Luo uuden käyttöoikeusryhmän.",
            notes = "Tekee uuden käyttöoikeusryhmän annetun DTO:n pohjalta.")
    public Response createKayttoOikeusRyhma(@RequestBody KayttoOikeusRyhmaModifyDto uusiRyhma) {
        try {
            KayttoOikeusRyhmaDto created = kayttoOikeusService.createKayttoOikeusRyhma(uusiRyhma);
            accessRightAuditLogger.auditModifyAccessRightGroupData(getCurrentUserOid(), "NEW", true);
            return Response.ok(created.getId()).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing mandatory parameters: " + iae.getMessage()).build();
        } catch (Exception e) {
            logger.error("Error creating new access right group", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unknown internal error").build();
        }
    }


    @RequestMapping(value = "/kayttooikeus", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ROLE_APP_KOOSTEROOLIENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Luo uuden käyttöoikeuden.",
            notes = "Luo uuden käyttöoikeuden annetun käyttöoikeus modelin pohjalta.")
    public KayttoOikeusDto createNewKayttoOikeus(@RequestBody KayttoOikeusDto kayttoOikeus) {
        return kayttoOikeusService.createKayttoOikeus(kayttoOikeus);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ROLE_APP_KOOSTEROOLIENHALLINTA_CRUD',"
            + "'ROLE_APP_HENKILONHALLINTA_OPHREKISTERI')")
    @ApiOperation(value = "Päivittää käyttöoikeusryhmän.",
            notes = "Päivittää käyttöoikeusryhmän tiedot annetun DTO:n avulla.")
    public Response updateKayttoOikeusRyhma(@PathVariable("id") Long id, @RequestBody KayttoOikeusRyhmaModifyDto ryhmaData) {
        try {
            KayttoOikeusRyhmaDto koryhma = kayttoOikeusService.updateKayttoOikeusForKayttoOikeusRyhma(id, ryhmaData);
            accessRightAuditLogger.auditModifyAccessRightGroupData(getCurrentUserOid(), Long.toString(id), false);
            return Response.ok(koryhma).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("Error updating access right group", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unknown internal error").build();
        }
    }


    private String getCurrentUserOid() {
        String oid = SecurityContextHolder.getContext().getAuthentication().getName();
        if (oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }
}
