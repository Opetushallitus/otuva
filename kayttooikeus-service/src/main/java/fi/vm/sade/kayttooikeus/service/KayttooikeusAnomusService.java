package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;

import java.util.List;
import org.joda.time.LocalDate;

public interface KayttooikeusAnomusService {
    List<HaettuKayttooikeusryhmaDto> getAllActiveAnomusByHenkiloOid(String oidHenkilo, boolean activeOnly);

    void updateHaettuKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto);

    void grantKayttooikeusryhma(String anojaOid, String organisaatioOid, List<GrantKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList);

    Long createKayttooikeusAnomus(String anojaOid, KayttooikeusAnomusDto kayttooikeusAnomusDto);

    void cancelKayttooikeusAnomus(Long kayttooikeusRyhmaId);

    void lahetaUusienAnomuksienIlmoitukset(LocalDate anottuPvm);
}
