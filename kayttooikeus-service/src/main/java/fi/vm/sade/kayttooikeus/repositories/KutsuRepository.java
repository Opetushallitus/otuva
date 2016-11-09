package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.dto.KutsuOrganisaatioListDto;
import fi.vm.sade.kayttooikeus.model.Kutsu;

import java.util.List;

public interface KutsuRepository extends BaseRepository<Kutsu> {
    enum KutsuOrganisaatioOrder {
        SAHKOPOSTI,
        ORGANISAATIO,
        AIKALEIMA
    }
    
    List<KutsuListDto> listKutsuListDtos(KutsuCriteria criteria);

    List<KutsuOrganisaatioListDto> listKutsuOrganisaatioListDtos(KutsuCriteria kutsuCriteria, OrderBy<KutsuOrganisaatioOrder> orderBy);
}
