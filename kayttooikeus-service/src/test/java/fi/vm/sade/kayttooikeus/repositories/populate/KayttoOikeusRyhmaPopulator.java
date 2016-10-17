package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.KayttoOikeusRyhma;
import fi.vm.sade.kayttooikeus.model.TextGroup;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class KayttoOikeusRyhmaPopulator implements Populator<KayttoOikeusRyhma> {
    private final String name;
    private final List<Populator<KayttoOikeus>> oikeus = new ArrayList<>();

    public KayttoOikeusRyhmaPopulator(String name) {
        this.name = name;
    }
    
    public static KayttoOikeusRyhmaPopulator kayttoOikeusRyhma(String name) {
        return new KayttoOikeusRyhmaPopulator(name);
    }
    
    public KayttoOikeusRyhmaPopulator withOikeus(Populator<KayttoOikeus> oikeus) {
        this.oikeus.add(oikeus);
        return this;
    }

    @Override
    public KayttoOikeusRyhma apply(EntityManager entityManager) {
        KayttoOikeusRyhma ryhma = Populator.<KayttoOikeusRyhma>firstOptional(entityManager
                .createQuery("select kor from KayttoOikeusRyhma kor " +
                    "where kor.name = :name").setParameter("name", name)).orElseGet(() -> {
            KayttoOikeusRyhma r = new KayttoOikeusRyhma();
            r.setDescription(new TextGroup());
            r.setHidden(false);
            r.setName(name);
            entityManager.persist(r);
            return r;
        });
        
        oikeus.forEach(o -> {
            ryhma.getKayttoOikeus().add(o.apply(entityManager));
        });
        entityManager.merge(ryhma);
        
        return ryhma;
    }
}
