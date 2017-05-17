package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.model.Anomus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

import java.util.List;

public interface KayttooikeusAnomusService {
    List<HaettuKayttooikeusryhmaDto> getAllActiveAnomusByHenkiloOid(String oidHenkilo, boolean activeOnly);

    void updateHaettuKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto);

    void grantKayttooikeusryhma(String anojaOid, String organisaatioOid, List<GrantKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList);

    Long createKayttooikeusAnomus(String anojaOid, KayttooikeusAnomusDto kayttooikeusAnomusDto);

    void cancelKayttooikeusAnomus(Long kayttooikeusRyhmaId);
}
