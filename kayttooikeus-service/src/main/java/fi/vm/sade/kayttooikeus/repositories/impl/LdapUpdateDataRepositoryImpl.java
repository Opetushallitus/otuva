package fi.vm.sade.kayttooikeus.repositories.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import fi.vm.sade.kayttooikeus.model.QLdapUpdateData;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdateDataCriteria;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaContext;
import fi.vm.sade.kayttooikeus.repositories.LdapUpdateDataRepositoryCustom;

public class LdapUpdateDataRepositoryImpl implements LdapUpdateDataRepositoryCustom {

    private final EntityManager entityManager;

    public LdapUpdateDataRepositoryImpl(JpaContext jpaContext) {
        this.entityManager = jpaContext.getEntityManagerByManagedType(LdapUpdateData.class);
    }

    @Override
    public List<LdapUpdateData> findBy(LdapUpdateDataCriteria criteria, Long limit) {
        QLdapUpdateData qLdapUpdateData = QLdapUpdateData.ldapUpdateData;

        JPAQuery<LdapUpdateData> query = new JPAQuery<>(entityManager)
                .from(qLdapUpdateData)
                .where(criteria.condition(qLdapUpdateData))
                .select(qLdapUpdateData);
        if (limit != null) {
            query.limit(limit);
        }
        return query.fetch();
    }

}
