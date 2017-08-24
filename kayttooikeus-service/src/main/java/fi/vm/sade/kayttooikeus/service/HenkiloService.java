package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.HenkiloReadDto;
import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.enumeration.OrderByHenkilohaku;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeudetDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HenkiloService {

    /**
     * Palauttaa henkilön tiedot käyttäjätunnuksen perusteella.
     *
     * @param kayttajatunnus käyttäjätunnus
     * @return henkilö
     */
    HenkiloReadDto getByKayttajatunnus(String kayttajatunnus);

    /**
     * Palauttaa henkilöiden oid:t joiden tietoihin annetulla henkilöllä on
     * oikeutus.
     *
     * @param henkiloOid henkilö oid jonka oikeutuksia tarkistetaan
     * @param criteria haun lisäehdot
     * @return sallitut henkilö oid:t
     */
    KayttooikeudetDto getKayttooikeudet(String henkiloOid, OrganisaatioHenkiloCriteria criteria);

    void disableHenkiloOrganisationsAndKayttooikeus(String henkiloOid, String kasittelijaOid);

    List<HenkilohakuResultDto> henkilohaku(HenkilohakuCriteriaDto henkilohakuCriteriaDto, Long offset, OrderByHenkilohaku orderBy);

    boolean isVahvastiTunnistettu(String oidHenkilo);

    boolean isVahvastiTunnistettuByUsername(String username);
}
