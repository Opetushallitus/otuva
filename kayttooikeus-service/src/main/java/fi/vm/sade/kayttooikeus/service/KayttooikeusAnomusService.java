package fi.vm.sade.kayttooikeus.service;

import fi.vm.sade.kayttooikeus.dto.*;
import fi.vm.sade.kayttooikeus.enumeration.OrderByAnomus;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KayttooikeusAnomusService {

    List<HaettuKayttooikeusryhmaDto> listHaetutKayttoOikeusRyhmat(AnomusCriteria criteria, Long limit, Long offset, OrderByAnomus orderBy);

    void updateHaettuKayttooikeusryhma(UpdateHaettuKayttooikeusryhmaDto updateHaettuKayttooikeusryhmaDto);

    void grantKayttooikeusryhma(String anojaOid, String organisaatioOid, List<GrantKayttooikeusryhmaDto> updateHaettuKayttooikeusryhmaDtoList);

    Long createKayttooikeusAnomus(String anojaOid, KayttooikeusAnomusDto kayttooikeusAnomusDto);

    void cancelKayttooikeusAnomus(Long kayttooikeusRyhmaId);

    void lahetaUusienAnomuksienIlmoitukset(LocalDate anottuPvm);

    void removePrivilege(String oidHenkilo, Long id, String organisaatioOid);

    Map<String, Set<Long>> findCurrentHenkiloCanGrant(String accessedHenkiloOid);
}
