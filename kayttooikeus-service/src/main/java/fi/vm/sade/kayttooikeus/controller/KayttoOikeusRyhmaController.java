package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kayttooikeusryhma")
@Api(value = "/kayttooikeusryhma", description = "Käyttöoikeusryhmien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusRyhmaController {

    private KayttoOikeusRyhmaService kayttoOikeusRyhmaService;

    private static final Logger logger = LoggerFactory.getLogger(KayttoOikeusRyhmaController.class);

    @Autowired
    public KayttoOikeusRyhmaController(KayttoOikeusRyhmaService kayttoOikeusRyhmaService) {
        this.kayttoOikeusRyhmaService = kayttoOikeusRyhmaService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Listaa kaikki käyttöoikeusryhmät.2",
            notes = "Listaa kaikki käyttöoikeusryhmät, jotka on tallennettu henkilöhallintaan.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhma() {
        return kayttoOikeusRyhmaService.listAllKayttoOikeusRyhmas();
    }

    @RequestMapping(value = "/organisaatio/{organisaatioOid}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    @ApiOperation(value = "Listaa käyttöoikeusryhmät organisaation mukaan.",
            notes = "Listaa käyttöoikeusryhmät, jotka ovat mahdollisia pyynnössä annetulle organisaatiolle.")
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhmasByOrdOid(@PathVariable("organisaatioOid") String organisaatioOid) {
        return kayttoOikeusRyhmaService.listPossibleRyhmasByOrganization(organisaatioOid);
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
    public List<MyonnettyKayttoOikeusDTO> listKayttoOikeusRyhmasIncludingHenkilos(
            @PathVariable("oid") String oid, @PathVariable("organisaatioOid") String organisaatioOid) {
        List<MyonnettyKayttoOikeusDTO> results = null;
        try {
            results = kayttoOikeusRyhmaService.listMyonnettyKayttoOikeusRyhmasMergedWithHenkilos(
                    oid, organisaatioOid, getCurrentUserOid());
        }
        catch (Exception e) {
            logger.error("Error getting access right groups", e);
        }

        return results;
    }

    private String getCurrentUserOid() throws NullPointerException {
        String oid =  SecurityContextHolder.getContext().getAuthentication().getName();
        if(oid == null) {
            throw new NullPointerException("No user name available from SecurityContext!");
        }
        return oid;
    }
}
