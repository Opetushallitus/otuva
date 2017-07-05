package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Kayttajatiedot;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.first;

public class HenkiloPopulator implements Populator<Henkilo> {
    private final String oid;
    private String etunimetCached;
    private String sukunimiCached;
    private Boolean passivoituCached;
    private Boolean duplikaattiCached;
    private Kayttajatiedot kayttajatiedot;

    public HenkiloPopulator(String oid) {
        this.oid = oid;
    }

    public static HenkiloPopulator henkilo(String oid) {
        return new HenkiloPopulator(oid);
    }

    public HenkiloPopulator withNimet(String etunimi, String sukunimi) {
        this.etunimetCached = etunimi;
        this.sukunimiCached = sukunimi;
        return this;
    }

    public HenkiloPopulator withPassive(Boolean passivoitu) {
        this.passivoituCached = passivoitu;
        return this;
    }

    public HenkiloPopulator withDuplikate(Boolean duplikaatti) {
        this.duplikaattiCached = duplikaatti;
        return this;
    }

    public HenkiloPopulator withUsername(String username) {
        this.kayttajatiedot = new Kayttajatiedot();
        this.kayttajatiedot.setUsername(username);
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
        henkilo.setEtunimetCached(this.etunimetCached);
        henkilo.setSukunimiCached(this.sukunimiCached);
        henkilo.setPassivoituCached(this.passivoituCached);
        henkilo.setDuplicateCached(this.duplikaattiCached);
        if(this.kayttajatiedot != null) {
            this.kayttajatiedot.setHenkilo(henkilo);
            henkilo.setKayttajatiedot(this.kayttajatiedot);
        }
        entityManager.persist(henkilo);
        return entityManager.find(Henkilo.class, henkilo.getId());
    }
}
