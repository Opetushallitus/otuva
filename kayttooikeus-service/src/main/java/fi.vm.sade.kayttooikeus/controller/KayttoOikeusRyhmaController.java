package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.service.KayttoOikeusRyhmaService;
import fi.vm.sade.kayttooikeus.service.dto.KayttoOikeusRyhmaDto;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by autio on 4.10.2016.
 */
@RestController
@RequestMapping("/kayttooikeusryhma")
@Api(value = "/kayttooikeusryhma", description = "Käyttöoikeusryhmien käsittelyyn liittyvät operaatiot.")
public class KayttoOikeusRyhmaController {

    @Autowired
    private KayttoOikeusRyhmaService kayttoOikeusRyhmaService;
    
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(method = RequestMethod.GET)
    public List<KayttoOikeusRyhmaDto> listKayttoOikeusRyhma() {
        return kayttoOikeusRyhmaService.listAllKayttoOikeusRyhmas();
    }
}
