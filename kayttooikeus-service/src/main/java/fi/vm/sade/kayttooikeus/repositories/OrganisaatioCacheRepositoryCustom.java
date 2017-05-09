package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import java.util.Collection;

public interface OrganisaatioCacheRepositoryCustom {

    void persistInBatch(Collection<OrganisaatioCache> entities, int batchSize);

}
