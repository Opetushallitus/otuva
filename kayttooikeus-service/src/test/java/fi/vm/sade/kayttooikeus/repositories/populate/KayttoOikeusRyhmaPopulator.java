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
        KayttoOikeusRyhma ryhma = new KayttoOikeusRyhma();
        ryhma.setDescription(new TextGroup());
        ryhma.setHidden(false);
        ryhma.setName(name);
        entityManager.persist(ryhma);
        
        oikeus.forEach(o -> {
            o.apply(entityManager).getKayttooikeusRyhmas().add(ryhma);
        });
        
        return ryhma;
    }
}
