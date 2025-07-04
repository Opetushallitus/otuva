package fi.vm.sade.kayttooikeus.service;

import java.util.List;

import fi.vm.sade.kayttooikeus.controller.PalvelukayttajaController.Jarjestelmatunnus;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCreateDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaCriteriaDto;
import fi.vm.sade.kayttooikeus.dto.PalvelukayttajaReadDto;

/**
 * Palvelukäyttäjiin liittyvät toiminnot.
 *
 * @see HenkiloService yleiskäyttöisempi palvelu henkilöiden käsittelyyn
 */
public interface PalvelukayttajaService {

    /**
     * Palauttaa palvelukäyttäjät.
     *
     * @param criteria hakukriteerit
     * @return palvelukäyttäjät
     */
    List<PalvelukayttajaReadDto> list(PalvelukayttajaCriteriaDto criteria);

    /**
     * Luo palvelukäyttäjän.
     *
     * @param dto luotavan palvelukäyttäjän tiedot
     * @return luodun palvelukäyttäjän tiedot
     */
    Jarjestelmatunnus create(PalvelukayttajaCreateDto dto);

    String createCasPassword(String oid);

    String createOauth2ClientSecret(String oid);

    Jarjestelmatunnus getJarjestelmatunnus(String oid);

}
