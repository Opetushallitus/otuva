package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KutsuCreateDto;
import fi.vm.sade.kayttooikeus.dto.KutsuReadDto;
import fi.vm.sade.kayttooikeus.enumeration.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.repositories.criteria.KutsuCriteria;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface KutsuService {
    List<KutsuReadDto> listKutsus(KutsuOrganisaatioOrder sortBy, Sort.Direction direction, KutsuCriteria kutsuListCriteria, Long offset, Long amount);

    long createKutsu(KutsuCreateDto dto);

    KutsuReadDto getKutsu(Long id);

    Kutsu deleteKutsu(long id);

    KutsuReadDto getByTemporaryToken(String temporaryToken);
}
