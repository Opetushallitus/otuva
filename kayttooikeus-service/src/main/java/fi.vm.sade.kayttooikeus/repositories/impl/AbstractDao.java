package fi.vm.sade.kayttooikeus.repositories.impl;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.sql.JPASQLQuery;
import com.mysema.query.sql.PostgresTemplates;
import com.mysema.query.types.EntityPath;

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

    protected JPAQuery jpa() {
        return new JPAQuery(em);
    }

    protected JPASQLQuery sql() {
        return new JPASQLQuery(em, PostgresTemplates.DEFAULT);
    }

    protected JPASQLQuery from(EntityPath<?>... o) {
        return sql().from(o);
    }
}
