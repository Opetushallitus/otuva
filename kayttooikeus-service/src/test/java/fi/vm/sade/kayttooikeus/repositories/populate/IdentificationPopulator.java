package fi.vm.sade.kayttooikeus.repositories.populate;

import fi.vm.sade.kayttooikeus.model.Henkilo;
import fi.vm.sade.kayttooikeus.model.Identification;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Date;

public class IdentificationPopulator implements Populator<Identification> {
    private HenkiloPopulator henkilo;
    private String idpEntityId;
    private String identifier;
    private Date expirationDate;
    private String authToken;

    public IdentificationPopulator(String idpEntityId, String identifier, HenkiloPopulator henkilo) {
        this.idpEntityId = idpEntityId;
        this.identifier = identifier;
        this.henkilo = henkilo;
    }

    public static IdentificationPopulator identification(String idpEntityId, String identifier, HenkiloPopulator henkilo) {
        return new IdentificationPopulator(idpEntityId, identifier, henkilo);
    }

    public IdentificationPopulator withExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public IdentificationPopulator withAuthToken(String token) {
        this.authToken = token;
        return this;
    }

    @Override
    public Identification apply(EntityManager entityManager) {
        Henkilo henkiloo = henkilo.apply(entityManager);
        Identification identification = new Identification();
        identification.setIdentifier(identifier);
        identification.setIdpEntityId(idpEntityId);
        identification.setExpirationDate(expirationDate);
        identification.setAuthtoken(authToken);
        identification.setHenkilo(henkiloo);
        entityManager.persist(identification);
        henkiloo.setIdentifications(Collections.singleton(identification));
        return entityManager.find(Identification.class, identification.getId());
    }
}
