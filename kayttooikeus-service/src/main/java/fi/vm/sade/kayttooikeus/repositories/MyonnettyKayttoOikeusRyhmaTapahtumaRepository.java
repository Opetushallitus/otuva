package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.service.dto.MyonnettyKayttoOikeusDto;

import java.util.List;

public interface MyonnettyKayttoOikeusRyhmaTapahtumaRepository {
    List<Long> findMasterIdsByHenkilo(String henkiloOid);

    List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid);
}
