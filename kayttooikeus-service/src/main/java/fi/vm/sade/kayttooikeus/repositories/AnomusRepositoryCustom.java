package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Anomus;
import java.util.List;

public interface AnomusRepositoryCustom {

    List<Anomus> findBy(AnomusCriteria criteria);

}
