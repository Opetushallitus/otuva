package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.LdapUpdateData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LdapUpdaterRepository extends QueryDslPredicateExecutor, CrudRepository<LdapUpdateData, Long> {
    List<LdapUpdateData> findBy(String korId, Long id);

    @Query("select count(ldap) from LdapUpdateData ldap where ldap.korId = ?1")
    Long numberOfUpdatesForKor(long korId);
}
