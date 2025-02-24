package fi.vm.sade.kayttooikeus.service.it;

import fi.vm.sade.kayttooikeus.repositories.populate.Populator;
import fi.vm.sade.kayttooikeus.service.AbstractServiceTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;

@Transactional
public abstract class AbstractServiceIntegrationTest extends AbstractServiceTest {
    @PersistenceContext
    protected EntityManager em;

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    }

    protected<T> T populate(Populator<T> populator) {
        T entity = populator.apply(em);
        em.flush();
        return entity;
    }
}
