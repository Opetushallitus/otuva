package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.dto.MyonnettyKayttoOikeusDto;

import java.util.List;

public interface KayttoOikeusRyhmaTapahtumaHistoriaRepository {
    List<MyonnettyKayttoOikeusDto> findByHenkiloInOrganisaatio(String henkiloOid, String organisaatioOid);
}
