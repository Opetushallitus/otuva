package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.OrganisaatioViite;
import fi.vm.sade.kayttooikeus.model.TextGroup;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class KayttoOikeusRyhmaPopulator implements Populator<KayttoOikeusRyhma> {
    private final String name;
    private boolean hidden;
    private final List<Populator<OrganisaatioViite>> viitteet = new ArrayList<>();
    private final List<Populator<KayttoOikeus>> oikeus = new ArrayList<>();
    private Populator<TextGroup> kuvaus = Populator.constant(new TextGroup());

    public KayttoOikeusRyhmaPopulator(String name) {
        this.name = name;
        this.hidden = false;
    }
    
    public static KayttoOikeusRyhmaPopulator kayttoOikeusRyhma(String name) {
        return new KayttoOikeusRyhmaPopulator(name);
    }
    
    public KayttoOikeusRyhmaPopulator withOikeus(Populator<KayttoOikeus> oikeus) {
        this.oikeus.add(oikeus);
        return this;
    }
    
    public KayttoOikeusRyhmaPopulator withKuvaus(Populator<TextGroup> kuvaus) {
        this.kuvaus = kuvaus;
        return this;
    }
    
    public KayttoOikeusRyhmaPopulator withViite(Populator<OrganisaatioViite> viite) {
        this.viitteet.add(viite);
        return this;
    }

    public KayttoOikeusRyhmaPopulator asHidden(){
        this.hidden = true;
        return this;
    }
    
    public static Populator<OrganisaatioViite> viite(Populator<KayttoOikeusRyhma> ryhma, String organisaatioTyyppi) {
        return em -> {
            OrganisaatioViite viite = new OrganisaatioViite();
            viite.setKayttoOikeusRyhma(ryhma.apply(em));
            viite.setOrganisaatioTyyppi(organisaatioTyyppi);
            em.persist(viite);
            return viite;
        };
    }

    @Override
    public KayttoOikeusRyhma apply(EntityManager entityManager) {
        KayttoOikeusRyhma ryhma = Populator.<KayttoOikeusRyhma>firstOptional(entityManager
                .createQuery("select kor from KayttoOikeusRyhma kor " +
                    "where kor.name = :name").setParameter("name", name)).orElseGet(() -> {
            KayttoOikeusRyhma r = new KayttoOikeusRyhma();
            r.setDescription(kuvaus.apply(entityManager));
            r.setName(name);
            r.setHidden(hidden);
            entityManager.persist(r);
            return r;
        });
        
        oikeus.forEach(o -> ryhma.getKayttoOikeus().add(o.apply(entityManager)));
        viitteet.forEach(v -> ryhma.getOrganisaatioViite().add(v.apply(entityManager)));
        entityManager.merge(ryhma);
        
        return ryhma;
    }
}
