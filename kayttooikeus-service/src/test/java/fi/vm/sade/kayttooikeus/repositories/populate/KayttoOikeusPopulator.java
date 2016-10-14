package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.KayttoOikeus;
import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.model.TextGroup;

import javax.persistence.EntityManager;

import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.constant;

public class KayttoOikeusPopulator implements Populator<KayttoOikeus> {
    private final Populator<Palvelu> palvelu;
    private final String rooli;

    public KayttoOikeusPopulator(Populator<Palvelu> palvelu, String rooli) {
        this.palvelu = palvelu;
        this.rooli = rooli;
    }

    public KayttoOikeusPopulator(String palveluName, String rooli) {
        this.palvelu = new PalveluPopulator(palveluName);
        this.rooli = rooli;
    }
    
    public static KayttoOikeusPopulator oikeus(Populator<Palvelu> palvelu, String rooli) {
        return new KayttoOikeusPopulator(palvelu, rooli);
    }

    public static KayttoOikeusPopulator oikeus(String palveluName, String rooli) {
        return new KayttoOikeusPopulator(palveluName, rooli);
    }

    @Override
    public KayttoOikeus apply(EntityManager entityManager) {
        KayttoOikeus oikeus = new KayttoOikeus();
        oikeus.setRooli(rooli);
        oikeus.setPalvelu(palvelu.apply(entityManager));
        oikeus.setTextGroup(new TextGroup());
        entityManager.persist(oikeus);
        
        return oikeus;
    }
}
