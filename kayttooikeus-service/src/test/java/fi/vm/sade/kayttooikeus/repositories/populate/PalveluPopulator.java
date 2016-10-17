package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Palvelu;
import fi.vm.sade.kayttooikeus.model.PalveluTyyppi;
import fi.vm.sade.kayttooikeus.model.TextGroup;

import javax.persistence.EntityManager;
import static fi.vm.sade.kayttooikeus.repositories.populate.Populator.first;

public class PalveluPopulator implements Populator<Palvelu> {
    private final String name;

    public PalveluPopulator(String name) {
        this.name = name;
    }
    
    public static PalveluPopulator palvelu(String name) {
        return new PalveluPopulator(name);
    }

    @Override
    public Palvelu apply(EntityManager entityManager) {
        Palvelu existing = first(entityManager.createQuery("select p from Palvelu p where p.name = :name")
                .setParameter("name", name));
        if (existing != null) {
            return existing;
        }
        
        Palvelu palvelu = new Palvelu();
        palvelu.setName(name);
        palvelu.setDescription(new TextGroup());
        palvelu.setPalveluTyyppi(PalveluTyyppi.YKSITTAINEN);
        entityManager.persist(palvelu);
        
        return palvelu;
    }
}
