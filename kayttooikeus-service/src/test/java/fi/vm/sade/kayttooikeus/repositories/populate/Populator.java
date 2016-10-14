package fi.vm.sade.kayttooikeus.repositories.populate;

import javax.persistence.EntityManager;
import java.util.function.Function;

@FunctionalInterface
public interface Populator<T> extends Function<EntityManager, T> {
    static<T> Populator<T> constant(T entity) {
        return em -> entity;
    }
}
