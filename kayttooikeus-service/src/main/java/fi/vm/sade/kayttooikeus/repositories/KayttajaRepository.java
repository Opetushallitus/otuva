package fi.vm.sade.kayttooikeus.repositories;

import fi.vm.sade.kayttooikeus.model.Kayttaja;
import java.util.List;
import java.util.Optional;
import org.springframework.data.ldap.repository.LdapRepository;

public interface KayttajaRepository extends LdapRepository<Kayttaja> {

    Optional<Kayttaja> findByKayttajatunnus(String kayttajatunnus);

    List<Kayttaja> findByOid(String oid);

}
