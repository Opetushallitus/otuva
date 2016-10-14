package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.model.PalveluTyyppi;
import fi.vm.sade.kayttooikeus.model.TextGroup;

import javax.persistence.EntityManager;

public class PalveluPopulator implements Populator<Palvelu> {
    private final String name;

    public PalveluPopulator(String name) {
        this.name = name;
    }

    @Override
    public Palvelu apply(EntityManager entityManager) {
        Palvelu palvelu = new Palvelu();
        palvelu.setName(name);
        palvelu.setDescription(new TextGroup());
        palvelu.setPalveluTyyppi(PalveluTyyppi.YKSITTAINEN);
        entityManager.persist(palvelu);
        
        return palvelu;
    }
}
