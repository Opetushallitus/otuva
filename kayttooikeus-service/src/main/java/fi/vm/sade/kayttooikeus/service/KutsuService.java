package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface KutsuService extends ExpiringEntitiesService<Kutsu> {
    List<KutsuReadDto> listKutsus(KutsuOrganisaatioOrder sortBy, Sort.Direction direction, KutsuCriteria kutsuListCriteria, Long offset, Long amount);

    /**
     * Uuden kutsun luominen
     *
     * @param dto kutsun luomiseen
     * @return Luodun kutsun id
     */
    long createKutsu(KutsuCreateDto dto);

    /**
     * Kutsun uusiminen muuttamatta kutsun sisältöä. Jos ei ole oma kutsu vaatii tavallisilta käyttäjiltä
     * authorisoinnin organisaatiohierarkian kautta.
     *
     * @param id kutsun ID
     */
    void renewKutsu(long id);

    /**
     * Merkitsee kutsun tilan poistetuksi. Ei fyysisesti poista mitään.
     *
     * @param id poistettavan kutsun id
     * @return poistetun kutsun id
     */
    Kutsu deleteKutsu(long id);
}
