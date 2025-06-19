package fi.vm.sade.kayttooikeus.controller;

import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;
import fi.vm.sade.kayttooikeus.service.PalvelukayttajaService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/palvelukayttaja", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PalvelukayttajaController {

    private final PalvelukayttajaService palvelukayttajaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public Iterable<PalvelukayttajaReadDto> list(PalvelukayttajaCriteriaDto criteria) {
        return palvelukayttajaService.list(criteria);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public Jarjestelmatunnus create(@RequestBody @Valid PalvelukayttajaCreateDto dto) {
        return palvelukayttajaService.create(dto);
    }

    @PutMapping(value = "/{oid}/cas", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public String createCasPassword(@PathVariable String oid) {
        return palvelukayttajaService.createCasPassword(oid);
    }

    @PutMapping(value = "/{oid}/oauth2", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public String createOauth2ClientSecret(@PathVariable String oid) {
        return palvelukayttajaService.createOauth2ClientSecret(oid);
    }

    @GetMapping(value = "/{oid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_APP_KAYTTOOIKEUS_PALVELUKAYTTAJA_CRUD', 'ROLE_APP_KAYTTOOIKEUS_REKISTERINPITAJA')")
    public Jarjestelmatunnus getJarjestelmatunnus(@PathVariable String oid) {
        return palvelukayttajaService.getJarjestelmatunnus(oid);
    }

    public record Jarjestelmatunnus(String oid, String nimi, String kayttajatunnus, List<Oauth2ClientCredential> oauth2Credentials) {};

    public record Oauth2ClientCredential(String clientId, LocalDateTime created, LocalDateTime updated, Kasittelija kasittelija) {};

    public record Kasittelija(String oid, String etunimet, String sukunimi, String kutsumanimi) {}
}