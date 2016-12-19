package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;

import javax.persistence.EntityManager;

public class KayttajatiedotPopulator implements Populator<Kayttajatiedot> {

    private final Populator<Henkilo> henkilo;
    private final String username;

    public KayttajatiedotPopulator(Populator<Henkilo> henkilo, String username) {
        this.henkilo = henkilo;
        this.username = username;
    }

    public static KayttajatiedotPopulator kayttajatiedot(Populator<Henkilo> henkilo, String username) {
        return new KayttajatiedotPopulator(henkilo, username);
    }

    @Override
    public Kayttajatiedot apply(EntityManager t) {
        Henkilo henkilo = this.henkilo.apply(t);
        Kayttajatiedot kayttajatiedot = new Kayttajatiedot();
        kayttajatiedot.setHenkilo(henkilo);
        kayttajatiedot.setUsername(username);
        t.persist(kayttajatiedot);
        henkilo.setKayttajatiedot(kayttajatiedot);
        return kayttajatiedot;
    }

}
