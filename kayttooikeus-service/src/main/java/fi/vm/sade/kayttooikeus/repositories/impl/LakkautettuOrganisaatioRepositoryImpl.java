package fi.vm.sade.kayttooikeus.repositories.impl;

import fi.vm.sade.kayttooikeus.model.LakkautettuOrganisaatio;
import fi.vm.sade.kayttooikeus.repositories.LakkautettuOrganisaatioRepositoryCustom;

import java.util.Collection;

public class LakkautettuOrganisaatioRepositoryImpl extends AbstractRepository implements LakkautettuOrganisaatioRepositoryCustom {
    @Override
    public void persistInBatch(Collection<String> oids, int batchSize) {
        int i = 0;
        for (String oid : oids) {
            i++;
            em.persist(new LakkautettuOrganisaatio(oid));
            if (i % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }
        if (i % batchSize != 0) {
            em.flush();
            em.clear();
        }
    }

}
