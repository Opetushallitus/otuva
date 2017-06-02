package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.HenkilohakuCriteriaDto;
import fi.vm.sade.kayttooikeus.repositories.dto.HenkilohakuResultDto;
import fi.vm.sade.kayttooikeus.dto.KayttooikeudetDto;
import fi.vm.sade.kayttooikeus.repositories.criteria.OrganisaatioHenkiloCriteria;
import fi.vm.sade.kayttooikeus.dto.OrganisaatioOidsSearchDto;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    List<HenkilohakuResultDto> henkilohaku(HenkilohakuCriteriaDto henkilohakuCriteriaDto);
}
