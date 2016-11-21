package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.dto.HenkiloTyyppi;
import fi.vm.sade.kayttooikeus.model.Henkilo;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.first;

public class HenkiloPopulator implements Populator<Henkilo> {
    private final String oid;
    private boolean passivoitu;

    public HenkiloPopulator(String oid) {
        this.passivoitu = false;
        this.oid = oid;
    }

    public static HenkiloPopulator henkilo(String oid) {
        return new HenkiloPopulator(oid);
    }

    public HenkiloPopulator withPassivoitu(boolean passivoitu) {
        this.passivoitu = passivoitu;
        return this;
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
        henkilo.setPassivoitu(passivoitu);
        henkilo.setHenkiloTyyppi(HenkiloTyyppi.VIRKAILIJA);
        entityManager.persist(henkilo);
        return entityManager.find(Henkilo.class, henkilo.getId());
    }
}
