package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;

import java.util.List;

public interface KutsuRepository {
    enum KutsuOrganisaatioOrder {
        SAHKOPOSTI,
        ORGANISAATIO,
        AIKALEIMA
    }
    
    List<KutsuListDto> listKutsuListDtos(KutsuCriteria criteria);

    List<KutsuOrganisaatioListDto> listKutsuOrganisaatioListDtos(KutsuCriteria kutsuCriteria, OrderBy<KutsuOrganisaatioOrder> orderBy);
}
