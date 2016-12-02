package fi.vm.sade.kayttooikeus.controller;


import fi.vm.sade.kayttooikeus.model.Kutsu;
import fi.vm.sade.kayttooikeus.model.KutsuOrganisaatio;
import fi.vm.sade.kayttooikeus.dto.KutsunTila;
import fi.vm.sade.kayttooikeus.repositories.populate.Populator;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KutsuPopulator implements Populator<Kutsu> {
    private final List<Populator<KutsuOrganisaatio>> organisaatiot = new ArrayList<>();
    private final String etunimi;
    private final String sukunimi;
    private final String sahkoposti;
    private String salaisuus = UUID.randomUUID().toString();
    private KutsunTila tila = KutsunTila.AVOIN;
    private String kieliKoodi = "FI";
    private String kutsuja = "kutsuja";
    private String luotuHenkiloOid;
    private DateTime aikaleima = DateTime.now();

    public KutsuPopulator(String etunimi, String sukunimi, String sahkoposti) {
        this.etunimi = etunimi;
        this.sukunimi = sukunimi;
        this.sahkoposti = sahkoposti;
    }
    
    public static KutsuPopulator kutsu(String etunimi, String sukunimi, String sahkoposti) {
        return new KutsuPopulator(etunimi, sukunimi, sahkoposti);
    }
    
    public KutsuPopulator kutsuja(String kutsuja) {
        this.kutsuja = kutsuja;
        return this;
    }

    public KutsuPopulator tila(KutsunTila tila) {
        this.tila = tila;
        return this;
    }
    
    public KutsuPopulator aikaleima(DateTime aikaleima) {
        this.aikaleima = aikaleima;
        return this;
    }

    public KutsuPopulator salaisuus(String salaisuus) {
        this.salaisuus = salaisuus;
        return this;
    }

    public KutsuPopulator organisaatio(Populator<KutsuOrganisaatio> kutsuOrganisaatio) {
        this.organisaatiot.add(kutsuOrganisaatio);
        return this;
    }
    
    public KutsuPopulator luotuHenkiloOid(String oid) {
        this.luotuHenkiloOid = oid;
        return this;
    }
    
    public KutsuPopulator kieliKoodi(String koodi) {
        this.kieliKoodi = koodi;
        return this;
    }
    
    @Override
    public Kutsu apply(EntityManager entityManager) {
        return Populator.<Kutsu>firstOptional(entityManager.createQuery("select k " +
                    "from Kutsu k where k.sahkoposti = :sahkoposti")
                .setParameter("sahkoposti", sahkoposti)).orElseGet(() -> {
            Kutsu kutsu = new Kutsu();
            kutsu.setEtunimi(etunimi);
            kutsu.setSukunimi(sukunimi);
            kutsu.setAikaleima(aikaleima);
            kutsu.setTila(tila);
            kutsu.setSahkoposti(sahkoposti);
            kutsu.setSalsisuus(salaisuus);
            kutsu.setKutsuja(kutsuja);
            kutsu.setLuotuHenkiloOid(luotuHenkiloOid);
            kutsu.setKieliKoodi(kieliKoodi);
            entityManager.persist(kutsu);
            
            organisaatiot.forEach(organisaatioPopulator -> {
                KutsuOrganisaatio kutsuOrganisaatio = organisaatioPopulator.apply(entityManager);
                kutsu.getOrganisaatiot().add(kutsuOrganisaatio);
            });
            
            return kutsu;
        });
    }
}
