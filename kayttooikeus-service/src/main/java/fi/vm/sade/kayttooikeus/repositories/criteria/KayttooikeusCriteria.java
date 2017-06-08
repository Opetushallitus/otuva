package fi.vm.sade.kayttooikeus.repositories.criteria;

import com.querydsl.core.types.Predicate;

public interface KayttooikeusCriteria<T> {
    Predicate condition(T object);
}
