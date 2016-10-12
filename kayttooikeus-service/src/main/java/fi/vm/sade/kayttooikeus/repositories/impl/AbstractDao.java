package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.PostgreSQLTemplates;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * User: tommiratamaa
 * Date: 2.9.2016
 * Time: 16.59
 */
public abstract class AbstractDao {
    @PersistenceContext
    protected EntityManager em;
    
    protected<T> JPAQuery<T> jpa() {
        return new JPAQuery<>(em);
    }

    protected<T> JPASQLQuery<T> sql() {
        return new JPASQLQuery<>(em, PostgreSQLTemplates.DEFAULT);
    }

    protected JPASQLQuery<Object> from(EntityPath<?>... o) {
        return sql().from(o);
    }

    protected boolean exists(JPAQuery<?> q) {
        return q.limit(1).fetchCount() > 0;
    }
    
    protected boolean exists(JPASQLQuery<?> q) {
        return q.limit(1).fetchCount() > 0;
    }
}
