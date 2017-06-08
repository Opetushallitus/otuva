package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Anomus;
import fi.vm.sade.kayttooikeus.repositories.criteria.AnomusCriteria;

import java.util.List;

public interface AnomusRepositoryCustom {

    List<Anomus> findBy(AnomusCriteria criteria);

}
