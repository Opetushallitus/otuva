package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public abstract class AbstractRepository {
    @PersistenceContext
    protected EntityManager em;

    protected JPAQueryFactory jpa() {
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, em);
    }

    protected boolean exists(JPAQuery<?> q) {
        return q.limit(1).fetchCount() > 0;
    }

}
