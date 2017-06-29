package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.HaettuKayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;
import java.util.List;

public interface HaettuKayttooikeusRyhmaRepositoryCustom {

    List<HaettuKayttoOikeusRyhma> findBy(AnomusCriteria criteria, Long limit, Long offset);

}
