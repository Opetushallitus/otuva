package fi.vm.sade.kayttooikeus.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import fi.vm.sade.kayttooikeus.config.properties.CommonProperties;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteria;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import fi.vm.sade.kayttooikeus.service.VirkailijaService;
import fi.vm.sade.kayttooikeus.service.exception.ValidationException;

@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class UiController {

    private final CommonProperties commonProperties;
    private final VirkailijaService virkailijaService;
    private final PalvelukayttajaService palvelukayttajaService;

    @PostMapping("/virkailijahaku")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_READ'," +
            "'ROLE_APP_KAYTTOOIKEUS_CRUD'," +
            "'ROLE_APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA_READ'," +
            "'ROLE_APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA'," +
            "'ROLE_APP_OPPIJANUMEROREKISTERI_READ'," +
            "'ROLE_APP_OPPIJANUMEROREKISTERI_HENKILON_RU'," +
            "'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public Set<HenkilohakuResultDto> virkailijahaku(@Validated @RequestBody HenkilohakuCriteria criteria) {
        if (!isHakuByName(criteria) && !isHakuByOrganisation(criteria) && criteria.getKayttooikeusryhmaId() == null) {
            throw new ValidationException("search by name, organisation oid, or kayttooikeusryhma id");
        }
        return virkailijaService.virkailijahaku(criteria);
    }

    @PostMapping("/jarjestelmatunnushaku")
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public Set<HenkilohakuResultDto> jarjestelmatunnushaku(@Validated @RequestBody HenkilohakuCriteria criteria) {
        if (!isHakuByName(criteria) && !isHakuByOrganisation(criteria) && criteria.getKayttooikeusryhmaId() == null) {
            throw new ValidationException("search by name or kayttooikeusryhma id");
        }
        return palvelukayttajaService.jarjestelmatunnushaku(criteria);
    }

    private boolean isHakuByName(HenkilohakuCriteria criteria) {
        return criteria.getNameQuery() != null && criteria.getNameQuery().length() >= 3;
    }

    private boolean isHakuByOrganisation(HenkilohakuCriteria criteria) {
        return !CollectionUtils.isEmpty(criteria.getOrganisaatioOids())
            && !(criteria.getOrganisaatioOids().contains(commonProperties.getRootOrganizationOid()) && criteria.getSubOrganisation());
    }
}
