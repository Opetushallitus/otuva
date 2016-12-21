package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.OrganisaatioCache;
import javax.persistence.EntityManager;

public class OrganisaatioCachePopulator implements Populator<OrganisaatioCache> {

    private final String organisaatioOid;

    public OrganisaatioCachePopulator(String organisaatioOid) {
        this.organisaatioOid = organisaatioOid;
    }

    public static OrganisaatioCachePopulator organisaatioCache(String organisaatioOid) {
        return new OrganisaatioCachePopulator(organisaatioOid);
    }

    @Override
    public OrganisaatioCache apply(EntityManager entityManager) {
        OrganisaatioCache organisaatioCache = new OrganisaatioCache(organisaatioOid, organisaatioOid);
        entityManager.persist(organisaatioCache);
        return organisaatioCache;
    }

}
