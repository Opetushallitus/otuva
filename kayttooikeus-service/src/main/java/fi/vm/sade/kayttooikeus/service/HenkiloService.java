package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KayttooikeudetDto;
import fi.vm.sade.kayttooikeus.repositories.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioOidsSearchDto;

import java.util.List;

public interface HenkiloService {

    /**
     * Palauttaa henkilöiden oid:t joiden tietoihin annetulla henkilöllä on
     * oikeutus.
     *
     * @param henkiloOid henkilö oid jonka oikeutuksia tarkistetaan
     * @param criteria haun lisäehdot
     * @return sallitut henkilö oid:t
     */
    KayttooikeudetDto getKayttooikeudet(String henkiloOid, OrganisaatioHenkiloCriteria criteria);

    List<String> findHenkilos(OrganisaatioOidsSearchDto organisaatioOidsSearchDto);

    void disableHenkiloOrganisationsAndKayttooikeus(String henkiloOid, String kasittelijaOid);
}
