package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.AbstractApplicationTest;
import fi.vm.sade.kayttooikeus.repositories.populate.Populator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;

@Transactional
public abstract class AbstractRepositoryTest extends AbstractApplicationTest {

    @BeforeAll
    public static void beforeClass() {
        System.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    }

    @PersistenceContext
    protected EntityManager em;

    protected<T> T populate(Populator<T> populator) {
        T entity = populator.apply(em);
        em.flush();
        return entity;
    }
}
