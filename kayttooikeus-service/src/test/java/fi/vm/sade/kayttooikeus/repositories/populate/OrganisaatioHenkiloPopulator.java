package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkilo;
import fi.vm.sade.kayttooikeus.model.OrganisaatioHenkiloTyyppi;

import javax.persistence.EntityManager;

public class OrganisaatioHenkiloPopulator implements Populator<OrganisaatioHenkilo> {
    private final Populator<Henkilo> henkilo;
    private final String organisaatioOid;

    public OrganisaatioHenkiloPopulator(String henkiloOid, String organisaatioOid) {
        this.henkilo = new HenkiloPopulator(henkiloOid);
        this.organisaatioOid = organisaatioOid;
    }

    public OrganisaatioHenkiloPopulator(Populator<Henkilo> henkilo, String organisaatioOid) {
        this.henkilo = henkilo;
        this.organisaatioOid = organisaatioOid;
    }

    public static OrganisaatioHenkiloPopulator organisaatioHenkilo(String henkiloOid, String organisaatioOid) {
        return new OrganisaatioHenkiloPopulator(henkiloOid, organisaatioOid);
    }
    public static OrganisaatioHenkiloPopulator organisaatioHenkilo(Populator<Henkilo> henkilo, String organisaatioOid) {
        return new OrganisaatioHenkiloPopulator(henkilo, organisaatioOid);
    }
    
    @Override
    public OrganisaatioHenkilo apply(EntityManager entityManager) {
        OrganisaatioHenkilo organisaatioHenkilo = new OrganisaatioHenkilo();
        organisaatioHenkilo.setHenkilo(henkilo.apply(entityManager));
        organisaatioHenkilo.setOrganisaatioOid(organisaatioOid);
        organisaatioHenkilo.setPassivoitu(false);
        organisaatioHenkilo.setOrganisaatioHenkiloTyyppi(OrganisaatioHenkiloTyyppi.VIRKAILIJA);
        entityManager.persist(organisaatioHenkilo);
        return organisaatioHenkilo;
    }
}
