package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.HaettuKayttooikeusryhmaDto;

import java.util.List;

public interface KayttooikeusAnomusService {
    List<HaettuKayttooikeusryhmaDto> getAllActiveAnomusByHenkiloOid(String oidHenkilo, boolean activeOnly);
}
