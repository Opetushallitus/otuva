package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.HenkiloTyyppi;

import javax.persistence.EntityManager;

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
        Henkilo henkilo = new Henkilo();
        henkilo.setOidHenkilo(oid);
        henkilo.setHenkiloTyyppi(HenkiloTyyppi.VIRKAILIJA);
        entityManager.persist(henkilo);
        return henkilo;
    }
}
