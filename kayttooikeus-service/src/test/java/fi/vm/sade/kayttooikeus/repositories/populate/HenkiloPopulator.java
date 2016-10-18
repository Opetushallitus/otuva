package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.first;

public class HenkiloPopulator implements Populator<Henkilo> {
    private final String oid;

    public HenkiloPopulator(String oid) {
        this.oid = oid;
    }
    
    public static HenkiloPopulator henkilo(String oid) {
        return new HenkiloPopulator(oid);
    }

    @Override
    public Henkilo apply(EntityManager entityManager) {
        Henkilo existing = first(entityManager.createQuery("select h from Henkilo h where h.oidHenkilo = :oid")
                    .setParameter("oid", oid));
        if (existing != null) {
            return existing;
        }
        Henkilo henkilo = new Henkilo();
        henkilo.setOidHenkilo(oid);
        henkilo.setHenkiloTyyppi(HenkiloTyyppi.VIRKAILIJA);
        entityManager.persist(henkilo);
        return entityManager.find(Henkilo.class, henkilo.getId());
    }
}
