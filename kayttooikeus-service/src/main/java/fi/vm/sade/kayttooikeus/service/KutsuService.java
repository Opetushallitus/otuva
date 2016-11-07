package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.KutsuListDto;
import fi.vm.sade.kayttooikeus.repositories.KutsuRepository.KutsuOrganisaatioOrder;
import fi.vm.sade.kayttooikeus.repositories.OrderBy;

import java.util.List;

public interface KutsuService {
    List<KutsuListDto> listAvoinKutsus(OrderBy<KutsuOrganisaatioOrder> orderBy);
}
